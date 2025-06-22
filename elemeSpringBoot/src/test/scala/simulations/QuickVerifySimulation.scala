package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * 快速验证测试
 * 用于验证Gatling配置和后端服务连接是否正常
 */
class QuickVerifySimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Quick Verify Test")

  // 简单的验证场景
  val quickVerifyScenario = scenario("快速验证测试")
    .exec(
      http("验证-获取验证码")
        .get("/api/captcha")
        .check(status.is(200))
        .check(jsonPath("$.id").exists)
        .check(jsonPath("$.imageBase64").exists)
        .check(responseTimeInMillis.lt(5000))
    )
    .pause(1.second)
    .exec(
      http("验证-获取所有商家")
        .post("/api/business/getAll")
        .check(status.is(200))
        .check(responseTimeInMillis.lt(3000))
    )
    .pause(1.second)
    .exec(
      http("验证-获取商品详情")
        .post("/api/food/getFoodById")
        .body(StringBody("""{"ID":1}"""))
        .check(status.in(200, 404))
        .check(responseTimeInMillis.lt(3000))
    )
    .pause(1.second)
    .exec(
      http("验证-获取购物车列表")
        .get("/api/cart/list")
        .queryParam("userPhone", "13800138000")
        .check(status.is(200))
        .check(responseTimeInMillis.lt(3000))
    )

  // 轻量级负载测试
  setUp(
    quickVerifyScenario.inject(
      rampUsers(5) during (10.seconds),
      constantUsersPerSec(2) during (30.seconds)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.max.lt(10000),
     global.responseTime.mean.lt(3000),
     global.successfulRequests.percent.gt(90),
     forAll.failedRequests.percent.lt(10)
   )
} 