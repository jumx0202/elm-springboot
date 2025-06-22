package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

/**
 * 饿了么系统综合性能测试
 * 模拟真实用户行为场景，包括：
 * 1. 用户注册登录
 * 2. 浏览商家和商品
 * 3. 购物车操作
 * 4. 下单支付流程
 * 5. 验证码功能
 */
class ElemeSystemTestSimulation extends Simulation {

  // HTTP协议配置
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Performance Test")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("zh-CN,zh;q=0.8,en;q=0.6")

  // 测试数据准备
  val phoneNumbers = (1 to 1000).map(i => f"1${Random.nextInt(9) + 1}%d${Random.nextInt(100000000)}%08d")
  val businessIds = (1 to 20).toList
  val foodIds = (1 to 100).toList

  // 用户手机号数据源
  val phoneFeeder = phoneNumbers.map(phone => Map("phone" -> phone)).toArray.circular
  
  // 业务ID数据源
  val businessFeeder = businessIds.map(id => Map("businessId" -> id)).toArray.circular
  
  // 商品ID数据源
  val foodFeeder = foodIds.map(id => Map("foodId" -> id)).toArray.circular

  // 场景1：用户注册登录流程
  val userAuthScenario = scenario("用户认证流程测试")
    .feed(phoneFeeder)
    .exec(
      http("获取验证码")
        .get("/api/captcha")
        .check(status.is(200))
        .check(jsonPath("$.id").saveAs("captchaId"))
    )
    .pause(1, 3)
    .exec(
      http("验证验证码")
        .post("/api/captcha")
        .body(StringBody("""{"id":"${captchaId}","value":"1234"}"""))
        .check(status.is(200))
    )
    .pause(1, 2)
    .exec(
      http("发送邮箱验证码")
        .post("/api/user/sendVerifyCode")
        .body(StringBody("""{"email":"test${phone}@example.com"}"""))
        .check(status.is(200))
    )
    .pause(2, 5)
    .exec(
      http("用户注册")
        .post("/api/user/register")
        .body(StringBody("""{"phoneNumber":"${phone}","password":"123456","email":"test${phone}@example.com","verifyCode":"123456"}"""))
        .check(status.in(200, 201))
    )
    .pause(1, 2)
    .exec(
      http("用户登录")
        .post("/api/user/login")
        .body(StringBody("""{"phoneNumber":"${phone}","password":"123456"}"""))
        .check(status.is(200))
        .check(jsonPath("$.token").optional.saveAs("userToken"))
    )

  // 场景2：商家和商品浏览
  val browseBusinessScenario = scenario("商家商品浏览测试")
    .feed(businessFeeder)
    .feed(foodFeeder)
    .exec(
      http("获取所有商家")
        .post("/api/business/getAll")
        .check(status.is(200))
        .check(jsonPath("$[*].id").findAll.optional.saveAs("allBusinessIds"))
    )
    .pause(1, 3)
    .exec(
      http("获取指定商家详情")
        .post("/api/business/getBusinessById")
        .body(StringBody("""{"ID":${businessId}}"""))
        .check(status.is(200))
        .check(jsonPath("$.data.foodList[*].id").findAll.optional.saveAs("businessFoodIds"))
    )
    .pause(1, 2)
    .exec(
      http("获取商品详情")
        .post("/api/food/getFoodById")
        .body(StringBody("""{"ID":${foodId}}"""))
        .check(status.in(200, 404))
    )
    .pause(1, 2)
    .exec(
      http("批量获取商品")
        .post("/api/food/getAllByIds")
        .body(StringBody("""{"ids":[${foodId}]}"""))
        .check(status.in(200, 204))
    )

  // 场景3：购物车操作流程
  val cartOperationScenario = scenario("购物车操作测试")
    .feed(phoneFeeder)
    .feed(foodFeeder)
    .feed(businessFeeder)
    .exec(
      http("添加商品到购物车")
        .post("/api/cart/add")
        .header("User-Phone", "${phone}")
        .body(StringBody("""{
          "userPhone":"${phone}",
          "foodId":${foodId},
          "foodName":"测试商品",
          "quantity":${__Random(1,5)},
          "unitPrice":${__Random(10,50)}.99,
          "totalPrice":${__Random(10,100)}.99,
          "businessId":${businessId},
          "businessName":"测试商家",
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.is(200))
        .check(jsonPath("$.code").is("200"))
    )
    .pause(1, 2)
    .exec(
      http("获取购物车列表")
        .get("/api/cart/list")
        .queryParam("userPhone", "${phone}")
        .queryParam("page", "1")
        .queryParam("size", "10")
        .check(status.is(200))
        .check(jsonPath("$.data.items[*].id").findAll.optional.saveAs("cartItemIds"))
    )
    .pause(1, 2)
    .doIf(session => session("cartItemIds").asOption[Vector[String]].exists(_.nonEmpty)) {
      exec(
        http("更新购物车商品数量")
          .put("/api/cart/update/${cartItemIds.random()}")
          .queryParam("quantity", "${__Random(1,10)}")
          .check(status.is(200))
      )
    }
    .pause(1, 2)
    .exec(
      http("验证购物车")
        .get("/api/cart/validate")
        .queryParam("userPhone", "${phone}")
        .check(status.is(200))
        .check(jsonPath("$.data.canCheckout").optional.saveAs("canCheckout"))
    )
    .pause(1, 2)
    .exec(
      http("获取购物车统计")
        .get("/api/cart/statistics")
        .queryParam("userPhone", "${phone}")
        .check(status.is(200))
    )

  // 场景4：订单处理流程
  val orderProcessScenario = scenario("订单处理测试")
    .feed(phoneFeeder)
    .feed(businessFeeder)
    .exec(
      http("创建订单")
        .post("/api/order/addUserOrder")
        .body(StringBody("""{
          "businessID":${businessId},
          "userPhone":"${phone}",
          "orderList":[${__Random(1,100)}],
          "price":${__Random(50,200)}.99
        }"""))
        .check(status.is(200))
        .check(bodyString.saveAs("orderId"))
    )
    .pause(1, 3)
    .doIf(session => session("orderId").asOption[String].exists(_.nonEmpty)) {
      exec(
        http("查询订单详情")
          .post("/api/order/getOrderDetail")
          .body(StringBody("""{"ID":${orderId}}"""))
          .check(status.in(200, 204))
      )
      .pause(1, 2)
      .exec(
        http("查询订单时间")
          .get("/api/order/getOrderTime/${orderId}")
          .check(status.in(200, 404))
      )
      .pause(2, 5)
      .exec(
        http("订单支付")
          .post("/api/order/havePayed")
          .body(StringBody("""{"ID":${orderId}}"""))
          .check(status.is(200))
      )
    }
    .pause(1, 2)
    .exec(
      http("获取用户所有订单")
        .post("/api/order/getAllUserOrder")
        .body(StringBody("""{"userPhone":"${phone}"}"""))
        .check(status.is(200))
    )

  // 场景5：高频API压力测试
  val highFrequencyScenario = scenario("高频API压力测试")
    .feed(businessFeeder)
    .feed(foodFeeder)
    .exec(
      http("获取验证码-高频")
        .get("/api/captcha")
        .check(status.is(200))
    )
    .pause(100.milliseconds, 500.milliseconds)
    .exec(
      http("商家列表-高频")
        .post("/api/business/getAll")
        .check(status.is(200))
    )
    .pause(100.milliseconds, 300.milliseconds)
    .exec(
      http("商品详情-高频")
        .post("/api/food/getFoodById")
        .body(StringBody("""{"ID":${foodId}}"""))
        .check(status.in(200, 404))
    )

  // 场景6：边界值和异常测试
  val boundaryTestScenario = scenario("边界值异常测试")
    .exec(
      http("无效商家ID测试")
        .post("/api/business/getBusinessById")
        .body(StringBody("""{"ID":99999}"""))
        .check(status.in(200, 404))
    )
    .pause(500.milliseconds)
    .exec(
      http("无效商品ID测试")
        .post("/api/food/getFoodById")
        .body(StringBody("""{"ID":99999}"""))
        .check(status.in(200, 404))
    )
    .pause(500.milliseconds)
    .exec(
      http("空参数测试")
        .post("/api/cart/add")
        .body(StringBody("{}"))
        .check(status.in(400, 500))
    )
    .pause(500.milliseconds)
    .exec(
      http("超长用户名测试")
        .post("/api/user/login")
        .body(StringBody(s"""{"phoneNumber":"${"1" * 20}","password":"123456"}"""))
        .check(status.in(400, 500))
    )

  // 场景7：并发购物车操作测试
  val concurrentCartScenario = scenario("并发购物车测试")
    .feed(phoneFeeder)
    .feed(foodFeeder)
    .feed(businessFeeder)
    .exec(
      http("并发添加购物车")
        .post("/api/cart/add")
        .body(StringBody("""{
          "userPhone":"${phone}",
          "foodId":${foodId},
          "foodName":"并发测试商品",
          "quantity":1,
          "unitPrice":19.99,
          "totalPrice":19.99,
          "businessId":${businessId},
          "createdTime":"2024-01-01T12:00:00",
          "isValid":1
        }"""))
        .check(status.is(200))
    )
    .pause(100.milliseconds)
    .exec(
      http("并发获取购物车")
        .get("/api/cart/list")
        .queryParam("userPhone", "${phone}")
        .check(status.is(200))
    )
    .pause(100.milliseconds)
    .exec(
      http("并发清空购物车")
        .delete("/api/cart/clear")
        .queryParam("userPhone", "${phone}")
        .check(status.is(200))
    )

  // 设置测试负载模式
  setUp(
    // 基础功能测试 - 模拟正常用户行为
    userAuthScenario.inject(
      rampUsers(20) during (30.seconds),
      constantUsersPerSec(5) during (2.minutes)
    ),
    
    // 商家商品浏览 - 高频访问
    browseBusinessScenario.inject(
      rampUsers(50) during (45.seconds),
      constantUsersPerSec(10) during (3.minutes)
    ),
    
    // 购物车操作 - 中等频率
    cartOperationScenario.inject(
      rampUsers(30) during (60.seconds),
      constantUsersPerSec(8) during (2.minutes)
    ),
    
    // 订单处理 - 关键业务流程
    orderProcessScenario.inject(
      rampUsers(25) during (45.seconds),
      constantUsersPerSec(6) during (2.minutes)
    ),
    
    // 高频API压力测试
    highFrequencyScenario.inject(
      rampUsers(100) during (30.seconds),
      constantUsersPerSec(20) during (1.minutes)
    ),
    
    // 边界值和异常测试
    boundaryTestScenario.inject(
      rampUsers(15) during (60.seconds),
      constantUsersPerSec(3) during (2.minutes)
    ),
    
    // 并发购物车测试
    concurrentCartScenario.inject(
      rampUsers(40) during (20.seconds),
      constantUsersPerSec(15) during (1.minutes)
    )
  ).protocols(httpProtocol)
   .assertions(
     // 全局性能指标
     global.responseTime.max.lt(5000),
     global.responseTime.mean.lt(2000),
     global.responseTime.percentile3.lt(3000),
     global.successfulRequests.percent.gt(95),
     
     // 具体场景性能要求
     forAll.responseTime.max.lt(10000),
     forAll.failedRequests.percent.lt(5)
   )
} 