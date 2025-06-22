package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

/**
 * 购物车模块专项负载测试
 * 重点测试购物车的高并发操作和数据一致性
 */
class CartLoadTestSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .header("User-Agent", "Gatling Cart Load Test")

  // 测试数据
  val testUsers = (1 to 500).map(i => f"1${Random.nextInt(9) + 1}%d${Random.nextInt(100000000)}%08d")
  val testBusinessIds = (1 to 50).toList
  val testFoodIds = (1 to 200).toList
  
  val userFeeder = testUsers.map(phone => Map("userPhone" -> phone)).toArray.circular
  val businessFeeder = testBusinessIds.map(id => Map("businessId" -> id)).toArray.circular
  val foodFeeder = testFoodIds.map(id => Map("foodId" -> id)).toArray.circular

  // 场景1：购物车基本操作压力测试
  val cartBasicOperationsScenario = scenario("购物车基本操作压力测试")
    .feed(userFeeder)
    .feed(businessFeeder)
    .feed(foodFeeder)
    .exec(
      http("添加商品到购物车")
        .post("/api/cart/add")
        .body(StringBody("""{
          "userPhone":"${userPhone}",
          "foodId":${foodId},
          "foodName":"压力测试商品_${foodId}",
          "quantity":${__Random(1,10)},
          "unitPrice":${__Random(15,80)}.99,
          "totalPrice":${__Random(15,800)}.99,
          "businessId":${businessId},
          "businessName":"测试商家_${businessId}",
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.is(200))
        .check(jsonPath("$.code").is("200"))
        .check(responseTimeInMillis.lt(3000))
    )
    .pause(200.milliseconds, 800.milliseconds)
    .exec(
      http("获取购物车列表")
        .get("/api/cart/list")
        .queryParam("userPhone", "${userPhone}")
        .queryParam("page", "1")
        .queryParam("size", "20")
        .check(status.is(200))
        .check(jsonPath("$.code").is("200"))
        .check(jsonPath("$.data.items").exists)
        .check(jsonPath("$.data.items[*].id").findAll.optional.saveAs("cartItemIds"))
        .check(responseTimeInMillis.lt(2000))
    )
    .pause(100.milliseconds, 500.milliseconds)
    .doIf(session => session("cartItemIds").asOption[Vector[String]].exists(_.nonEmpty)) {
      exec(
        http("更新购物车商品数量")
          .put("/api/cart/update/${cartItemIds.random()}")
          .queryParam("quantity", "${__Random(1,15)}")
          .check(status.is(200))
          .check(jsonPath("$.code").is("200"))
          .check(responseTimeInMillis.lt(2000))
      )
    }

  // 场景2：高并发购物车操作
  val highConcurrencyCartScenario = scenario("高并发购物车操作")
    .feed(userFeeder)
    .feed(businessFeeder)
    .feed(foodFeeder)
    .exec(
      http("并发添加购物车")
        .post("/api/cart/add")
        .body(StringBody("""{
          "userPhone":"${userPhone}",
          "foodId":${foodId},
          "foodName":"并发测试_${foodId}",
          "quantity":1,
          "unitPrice":29.99,
          "totalPrice":29.99,
          "businessId":${businessId},
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.is(200))
        .check(responseTimeInMillis.lt(5000))
    )
    .pause(50.milliseconds, 200.milliseconds)
    .repeat(3) {
      exec(
        http("快速获取购物车")
          .get("/api/cart/list")
          .queryParam("userPhone", "${userPhone}")
          .check(status.is(200))
          .check(responseTimeInMillis.lt(3000))
      )
      .pause(100.milliseconds, 300.milliseconds)
    }
    .exec(
      http("验证购物车状态")
        .get("/api/cart/validate")
        .queryParam("userPhone", "${userPhone}")
        .check(status.is(200))
        .check(jsonPath("$.data.canCheckout").exists)
        .check(responseTimeInMillis.lt(2000))
    )

  // 场景3：购物车数据一致性测试
  val cartConsistencyTestScenario = scenario("购物车数据一致性测试")
    .feed(userFeeder)
    .feed(businessFeeder)
    .feed(foodFeeder)
    .exec(
      http("添加商品A")
        .post("/api/cart/add")
        .body(StringBody("""{
          "userPhone":"${userPhone}",
          "foodId":${foodId},
          "foodName":"一致性测试商品A",
          "quantity":2,
          "unitPrice":25.00,
          "totalPrice":50.00,
          "businessId":${businessId},
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.is(200))
    )
    .pause(100.milliseconds)
    .exec(
      http("添加商品B")
        .post("/api/cart/add")
        .body(StringBody("""{
          "userPhone":"${userPhone}",
          "foodId":${__Random(1,200)},
          "foodName":"一致性测试商品B",
          "quantity":1,
          "unitPrice":35.00,
          "totalPrice":35.00,
          "businessId":${businessId},
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.is(200))
    )
    .pause(100.milliseconds)
    .exec(
      http("获取购物车统计")
        .get("/api/cart/statistics")
        .queryParam("userPhone", "${userPhone}")
        .check(status.is(200))
        .check(responseTimeInMillis.lt(2000))
    )
    .pause(200.milliseconds)
    .exec(
      http("验证总金额")
        .get("/api/cart/validate")
        .queryParam("userPhone", "${userPhone}")
        .check(status.is(200))
        .check(jsonPath("$.data.totalAmount").exists)
        .check(responseTimeInMillis.lt(2000))
    )

  // 场景4：购物车批量操作测试
  val cartBatchOperationScenario = scenario("购物车批量操作测试")
    .feed(userFeeder)
    .exec(
      http("批量清空购物车")
        .post("/api/cart/batch")
        .body(StringBody("""{
          "operation":"clear",
          "userPhone":"${userPhone}"
        }"""))
        .check(status.is(200))
        .check(responseTimeInMillis.lt(3000))
    )
    .pause(500.milliseconds)
    .exec(
      http("批量验证购物车")
        .post("/api/cart/batch")
        .body(StringBody("""{
          "operation":"validate",
          "userPhone":"${userPhone}"
        }"""))
        .check(status.is(200))
        .check(jsonPath("$.data.valid").exists)
        .check(responseTimeInMillis.lt(2000))
    )

  // 场景5：购物车边界值测试
  val cartBoundaryTestScenario = scenario("购物车边界值测试")
    .feed(userFeeder)
    .feed(businessFeeder)
    .feed(foodFeeder)
    .exec(
      http("最小数量商品")
        .post("/api/cart/add")
        .body(StringBody("""{
          "userPhone":"${userPhone}",
          "foodId":${foodId},
          "foodName":"边界测试_最小数量",
          "quantity":1,
          "unitPrice":0.01,
          "totalPrice":0.01,
          "businessId":${businessId},
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.is(200))
    )
    .pause(300.milliseconds)
    .exec(
      http("最大数量商品")
        .post("/api/cart/add")
        .body(StringBody("""{
          "userPhone":"${userPhone}",
          "foodId":${__Random(1,200)},
          "foodName":"边界测试_最大数量",
          "quantity":999,
          "unitPrice":9999.99,
          "totalPrice":9999.99,
          "businessId":${businessId},
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.in(200, 400))
    )
    .pause(300.milliseconds)
    .exec(
      http("无效商品ID")
        .post("/api/cart/add")
        .body(StringBody("""{
          "userPhone":"${userPhone}",
          "foodId":99999,
          "foodName":"边界测试_无效ID",
          "quantity":1,
          "unitPrice":19.99,
          "totalPrice":19.99,
          "businessId":99999,
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.in(200, 400, 404))
    )

  // 场景6：购物车删除操作测试
  val cartDeleteOperationScenario = scenario("购物车删除操作测试")
    .feed(userFeeder)
    .feed(businessFeeder)
    .feed(foodFeeder)
    .exec(
      http("添加测试商品")
        .post("/api/cart/add")
        .body(StringBody("""{
          "userPhone":"${userPhone}",
          "foodId":${foodId},
          "foodName":"删除测试商品",
          "quantity":2,
          "unitPrice":19.99,
          "totalPrice":39.98,
          "businessId":${businessId},
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.is(200))
    )
    .pause(500.milliseconds)
    .exec(
      http("获取购物车项ID")
        .get("/api/cart/list")
        .queryParam("userPhone", "${userPhone}")
        .check(status.is(200))
        .check(jsonPath("$.data.items[0].id").optional.saveAs("firstItemId"))
    )
    .pause(300.milliseconds)
    .doIf(session => session("firstItemId").asOption[String].exists(_.nonEmpty)) {
      exec(
        http("删除购物车商品")
          .delete("/api/cart/remove/${firstItemId}")
          .check(status.is(200))
          .check(jsonPath("$.code").is("200"))
          .check(responseTimeInMillis.lt(2000))
      )
    }
    .pause(500.milliseconds)
    .exec(
      http("清空购物车")
        .delete("/api/cart/clear")
        .queryParam("userPhone", "${userPhone}")
        .check(status.is(200))
        .check(responseTimeInMillis.lt(3000))
    )

  // 设置测试负载
  setUp(
    // 基本操作压力测试 - 模拟正常用户购物行为
    cartBasicOperationsScenario.inject(
      rampUsers(100) during (60.seconds),
      constantUsersPerSec(20) during (3.minutes),
      rampUsersPerSec(20) to 50 during (2.minutes),
      holdFor(2.minutes),
      rampUsersPerSec(50) to 10 during (1.minutes)
    ),
    
    // 高并发操作测试 - 模拟秒杀场景
    highConcurrencyCartScenario.inject(
      rampUsers(200) during (30.seconds),
      constantUsersPerSec(50) during (2.minutes)
    ),
    
    // 数据一致性测试 - 重点测试并发下的数据正确性
    cartConsistencyTestScenario.inject(
      rampUsers(80) during (45.seconds),
      constantUsersPerSec(15) during (3.minutes)
    ),
    
    // 批量操作测试
    cartBatchOperationScenario.inject(
      rampUsers(50) during (30.seconds),
      constantUsersPerSec(10) during (2.minutes)
    ),
    
    // 边界值测试
    cartBoundaryTestScenario.inject(
      rampUsers(30) during (60.seconds),
      constantUsersPerSec(5) during (2.minutes)
    ),
    
    // 删除操作测试
    cartDeleteOperationScenario.inject(
      rampUsers(40) during (45.seconds),
      constantUsersPerSec(8) during (2.minutes)
    )
  ).protocols(httpProtocol)
   .assertions(
     // 购物车性能指标
     global.responseTime.max.lt(8000),
     global.responseTime.mean.lt(2500),
     global.responseTime.percentile3.lt(4000),
     global.successfulRequests.percent.gt(95),
     
     // 具体操作性能要求
     details("添加商品到购物车").responseTime.max.lt(3000),
     details("获取购物车列表").responseTime.max.lt(2000),
     details("更新购物车商品数量").responseTime.max.lt(2000),
     details("购物车验证").responseTime.max.lt(2000),
     details("清空购物车").responseTime.max.lt(3000),
     
     // 错误率控制
     forAll.failedRequests.percent.lt(3)
   )
} 