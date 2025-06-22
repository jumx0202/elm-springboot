package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

/**
 * 订单业务流程专项测试
 * 重点测试完整的订单处理流程和业务逻辑
 */
class OrderWorkflowTestSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .header("User-Agent", "Gatling Order Workflow Test")

  // 测试数据
  val testUsers = (1 to 300).map(i => f"1${Random.nextInt(9) + 1}%d${Random.nextInt(100000000)}%08d")
  val testBusinessIds = (1 to 30).toList
  val testFoodIds = (1 to 150).toList
  
  val userFeeder = testUsers.map(phone => Map("userPhone" -> phone)).toArray.circular
  val businessFeeder = testBusinessIds.map(id => Map("businessId" -> id)).toArray.circular
  val foodFeeder = testFoodIds.map(id => Map("foodId" -> id)).toArray.circular

  // 场景1：完整订单流程测试
  val completeOrderWorkflowScenario = scenario("完整订单流程测试")
    .feed(userFeeder)
    .feed(businessFeeder)
    .feed(foodFeeder)
    .exec(
      http("步骤1-添加商品到购物车")
        .post("/api/cart/add")
        .body(StringBody("""{
          "userPhone":"${userPhone}",
          "foodId":${foodId},
          "foodName":"订单流程测试商品",
          "quantity":${__Random(1,5)},
          "unitPrice":${__Random(20,100)}.99,
          "totalPrice":${__Random(20,500)}.99,
          "businessId":${businessId},
          "businessName":"订单测试商家",
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.is(200))
        .check(jsonPath("$.code").is("200"))
    )
    .pause(1, 3)
    .exec(
      http("步骤2-验证购物车可结算")
        .get("/api/cart/validate")
        .queryParam("userPhone", "${userPhone}")
        .check(status.is(200))
        .check(jsonPath("$.data.canCheckout").is("true"))
        .check(jsonPath("$.data.totalAmount").saveAs("orderAmount"))
    )
    .pause(1, 2)
    .exec(
      http("步骤3-创建订单")
        .post("/api/order/addUserOrder")
        .body(StringBody("""{
          "businessID":${businessId},
          "userPhone":"${userPhone}",
          "orderList":[${foodId}],
          "price":${orderAmount}
        }"""))
        .check(status.is(200))
        .check(bodyString.saveAs("newOrderId"))
        .check(responseTimeInMillis.lt(3000))
    )
    .pause(2, 5)
    .doIf(session => session("newOrderId").asOption[String].exists(_.nonEmpty)) {
      exec(
        http("步骤4-查询订单详情")
          .post("/api/order/getOrderDetail")
          .body(StringBody("""{"ID":${newOrderId}}"""))
          .check(status.in(200, 204))
          .check(responseTimeInMillis.lt(2000))
      )
      .pause(1, 2)
      .exec(
        http("步骤5-查询订单时间")
          .get("/api/order/getOrderTime/${newOrderId}")
          .check(status.in(200, 404))
          .check(responseTimeInMillis.lt(1500))
      )
      .pause(3, 8)
      .exec(
        http("步骤6-订单支付")
          .post("/api/order/havePayed")
          .body(StringBody("""{"ID":${newOrderId}}"""))
          .check(status.is(200))
          .check(jsonPath("$").is("true"))
          .check(responseTimeInMillis.lt(2000))
      )
    }
    .pause(1, 2)
    .exec(
      http("步骤7-查询用户所有订单")
        .post("/api/order/getAllUserOrder")
        .body(StringBody("""{"userPhone":"${userPhone}"}"""))
        .check(status.is(200))
        .check(jsonPath("$").exists)
        .check(responseTimeInMillis.lt(2000))
    )

  // 场景2：订单并发创建测试
  val concurrentOrderCreationScenario = scenario("订单并发创建测试")
    .feed(userFeeder)
    .feed(businessFeeder)
    .exec(
      http("并发创建订单")
        .post("/api/order/addUserOrder")
        .body(StringBody("""{
          "businessID":${businessId},
          "userPhone":"${userPhone}",
          "orderList":[${__Random(1,100)}, ${__Random(1,100)}, ${__Random(1,100)}],
          "price":${__Random(50,300)}.99
        }"""))
        .check(status.is(200))
        .check(bodyString.saveAs("concurrentOrderId"))
        .check(responseTimeInMillis.lt(5000))
    )
    .pause(500.milliseconds, 2.seconds)
    .doIf(session => session("concurrentOrderId").asOption[String].exists(_.nonEmpty)) {
      exec(
        http("快速查询订单")
          .post("/api/order/getOrderDetail")
          .body(StringBody("""{"ID":${concurrentOrderId}}"""))
          .check(status.in(200, 204))
          .check(responseTimeInMillis.lt(3000))
      )
    }

  // 场景3：订单状态流转测试
  val orderStatusTransitionScenario = scenario("订单状态流转测试")
    .feed(userFeeder)
    .feed(businessFeeder)
    .exec(
      http("创建待支付订单")
        .post("/api/order/addUserOrder")
        .body(StringBody("""{
          "businessID":${businessId},
          "userPhone":"${userPhone}",
          "orderList":[${__Random(1,100)}],
          "price":${__Random(30,150)}.99
        }"""))
        .check(status.is(200))
        .check(bodyString.saveAs("statusOrderId"))
    )
    .pause(1, 3)
    .doIf(session => session("statusOrderId").asOption[String].exists(_.nonEmpty)) {
      exec(
        http("检查订单未支付状态")
          .post("/api/order/havePayed")
          .body(StringBody("""{"ID":${statusOrderId}}"""))
          .check(status.is(200))
          .check(jsonPath("$").is("false"))
      )
      .pause(2, 5)
      .exec(
        http("执行支付操作")
          .post("/api/order/havePayed")
          .body(StringBody("""{"ID":${statusOrderId}}"""))
          .check(status.is(200))
          .check(jsonPath("$").is("true"))
      )
      .pause(1, 2)
      .exec(
        http("验证支付后状态")
          .post("/api/order/getOrderDetail")
          .body(StringBody("""{"ID":${statusOrderId}}"""))
          .check(status.in(200, 204))
      )
    }

  // 场景4：大订单压力测试
  val largeOrderStressScenario = scenario("大订单压力测试")
    .feed(userFeeder)
    .feed(businessFeeder)
    .exec(
      http("创建大订单")
        .post("/api/order/addUserOrder")
        .body(StringBody("""{
          "businessID":${businessId},
          "userPhone":"${userPhone}",
          "orderList":[${__Random(1,100)}, ${__Random(1,100)}, ${__Random(1,100)}, ${__Random(1,100)}, ${__Random(1,100)}, ${__Random(1,100)}, ${__Random(1,100)}, ${__Random(1,100)}, ${__Random(1,100)}, ${__Random(1,100)}],
          "price":${__Random(500,2000)}.99
        }"""))
        .check(status.is(200))
        .check(bodyString.saveAs("largeOrderId"))
        .check(responseTimeInMillis.lt(8000))
    )
    .pause(1, 3)
    .exec(
      http("查询大订单详情")
        .post("/api/order/getOrderDetail")
        .body(StringBody("""{"ID":${largeOrderId}}"""))
        .check(status.in(200, 204))
        .check(responseTimeInMillis.lt(5000))
    )

  // 场景5：订单查询性能测试
  val orderQueryPerformanceScenario = scenario("订单查询性能测试")
    .feed(userFeeder)
    .exec(
      http("批量查询用户订单")
        .post("/api/order/getAllUserOrder")
        .body(StringBody("""{"userPhone":"${userPhone}"}"""))
        .check(status.is(200))
        .check(responseTimeInMillis.lt(2000))
    )
    .pause(300.milliseconds, 1.second)
    .repeat(5) {
      exec(
        http("随机查询订单详情")
          .post("/api/order/getOrderDetail")
          .body(StringBody("""{"ID":${__Random(1,1000)}}"""))
          .check(status.in(200, 204))
          .check(responseTimeInMillis.lt(2000))
      )
      .pause(200.milliseconds, 800.milliseconds)
    }

  // 场景6：订单边界值测试
  val orderBoundaryTestScenario = scenario("订单边界值测试")
    .feed(userFeeder)
    .feed(businessFeeder)
    .exec(
      http("最小金额订单")
        .post("/api/order/addUserOrder")
        .body(StringBody("""{
          "businessID":${businessId},
          "userPhone":"${userPhone}",
          "orderList":[1],
          "price":0.01
        }"""))
        .check(status.in(200, 400))
    )
    .pause(1.second)
    .exec(
      http("最大金额订单")
        .post("/api/order/addUserOrder")
        .body(StringBody("""{
          "businessID":${businessId},
          "userPhone":"${userPhone}",
          "orderList":[1],
          "price":9999.99
        }"""))
        .check(status.in(200, 400))
    )
    .pause(1.second)
    .exec(
      http("空商品列表订单")
        .post("/api/order/addUserOrder")
        .body(StringBody("""{
          "businessID":${businessId},
          "userPhone":"${userPhone}",
          "orderList":[],
          "price":100.00
        }"""))
        .check(status.in(200, 400))
    )
    .pause(1.second)
    .exec(
      http("无效商家ID订单")
        .post("/api/order/addUserOrder")
        .body(StringBody("""{
          "businessID":99999,
          "userPhone":"${userPhone}",
          "orderList":[1],
          "price":50.00
        }"""))
        .check(status.in(200, 400, 404))
    )

  // 场景7：订单异常处理测试
  val orderExceptionHandlingScenario = scenario("订单异常处理测试")
    .feed(userFeeder)
    .exec(
      http("查询不存在的订单")
        .post("/api/order/getOrderDetail")
        .body(StringBody("""{"ID":99999}"""))
        .check(status.in(200, 204, 404))
    )
    .pause(500.milliseconds)
    .exec(
      http("无效订单ID支付")
        .post("/api/order/havePayed")
        .body(StringBody("""{"ID":99999}"""))
        .check(status.in(200, 404))
    )
    .pause(500.milliseconds)
    .exec(
      http("无效用户手机号查询")
        .post("/api/order/getAllUserOrder")
        .body(StringBody("""{"userPhone":"invalid_phone"}"""))
        .check(status.in(200, 400))
    )
    .pause(500.milliseconds)
    .exec(
      http("查询不存在订单时间")
        .get("/api/order/getOrderTime/99999")
        .check(status.in(200, 404))
    )

  // 场景8：订单批量操作测试
  val orderBatchOperationScenario = scenario("订单批量操作测试")
    .feed(userFeeder)
    .feed(businessFeeder)
    .repeat(3) {
      exec(
        http("批量创建订单_${__counter()}")
          .post("/api/order/addUserOrder")
          .body(StringBody("""{
            "businessID":${businessId},
            "userPhone":"${userPhone}",
            "orderList":[${__Random(1,50)}, ${__Random(1,50)}],
            "price":${__Random(80,200)}.99
          }"""))
          .check(status.is(200))
      )
      .pause(200.milliseconds, 500.milliseconds)
    }
    .pause(1.second)
    .exec(
      http("批量查询用户订单")
        .post("/api/order/getAllUserOrder")
        .body(StringBody("""{"userPhone":"${userPhone}"}"""))
        .check(status.is(200))
        .check(jsonPath("$.length()").gte(3))
    )

  // 设置测试负载
  setUp(
    // 完整订单流程测试 - 模拟正常用户下单行为
    completeOrderWorkflowScenario.inject(
      rampUsers(60) during (60.seconds),
      constantUsersPerSec(12) during (3.minutes),
      rampUsersPerSec(12) to 25 during (2.minutes),
      holdFor(1.minutes),
      rampUsersPerSec(25) to 5 during (1.minutes)
    ),
    
    // 并发订单创建测试 - 模拟高峰期下单
    concurrentOrderCreationScenario.inject(
      rampUsers(100) during (30.seconds),
      constantUsersPerSec(30) during (2.minutes)
    ),
    
    // 订单状态流转测试 - 模拟支付流程
    orderStatusTransitionScenario.inject(
      rampUsers(40) during (45.seconds),
      constantUsersPerSec(10) during (2.minutes)
    ),
    
    // 大订单压力测试 - 测试系统处理能力
    largeOrderStressScenario.inject(
      rampUsers(20) during (60.seconds),
      constantUsersPerSec(5) during (2.minutes)
    ),
    
    // 订单查询性能测试 - 高频查询场景
    orderQueryPerformanceScenario.inject(
      rampUsers(80) during (30.seconds),
      constantUsersPerSec(20) during (2.minutes)
    ),
    
    // 边界值测试
    orderBoundaryTestScenario.inject(
      rampUsers(25) during (45.seconds),
      constantUsersPerSec(6) during (2.minutes)
    ),
    
    // 异常处理测试
    orderExceptionHandlingScenario.inject(
      rampUsers(30) during (60.seconds),
      constantUsersPerSec(8) during (2.minutes)
    ),
    
    // 批量操作测试
    orderBatchOperationScenario.inject(
      rampUsers(35) during (45.seconds),
      constantUsersPerSec(7) during (2.minutes)
    )
  ).protocols(httpProtocol)
   .assertions(
     // 订单系统性能指标
     global.responseTime.max.lt(10000),
     global.responseTime.mean.lt(3000),
     global.responseTime.percentile3.lt(5000),
     global.successfulRequests.percent.gt(93),
     
     // 具体操作性能要求
     details("创建订单").responseTime.max.lt(8000),
     details("查询订单详情").responseTime.max.lt(5000),
     details("订单支付").responseTime.max.lt(2000),
     details("查询用户所有订单").responseTime.max.lt(2000),
     details("查询订单时间").responseTime.max.lt(1500),
     
     // 业务流程性能要求
     details("步骤3-创建订单").responseTime.max.lt(3000),
     details("步骤6-订单支付").responseTime.max.lt(2000),
     
     // 错误率控制
     forAll.failedRequests.percent.lt(5)
   )
} 