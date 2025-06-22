package org.example;

import org.example.util.ValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能测试类
 * 测试验证工具类的性能表现
 */
@DisplayName("性能测试")
class PerformanceTest {

    private static final int ITERATIONS = 10000;
    private static final String VALID_PHONE = "13812345678";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "Test123";
    private static final String VALID_USERNAME = "张三";
    private static final String VALID_GENDER = "男";

    @Test
    @DisplayName("手机号验证性能测试")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testPhoneNumberValidationPerformance() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < ITERATIONS; i++) {
            ValidationUtils.ValidationResult result = ValidationUtils.validatePhoneNumber(VALID_PHONE);
            assertTrue(result.isSuccess());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("手机号验证 " + ITERATIONS + " 次耗时: " + duration + "ms");
        System.out.println("平均每次验证耗时: " + ((double)duration / ITERATIONS) + "ms");
        
        // 性能断言：平均每次验证应该少于1毫秒
        assertTrue(duration < ITERATIONS, "手机号验证性能应该满足要求");
    }

    @Test
    @DisplayName("邮箱验证性能测试")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testEmailValidationPerformance() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < ITERATIONS; i++) {
            ValidationUtils.ValidationResult result = ValidationUtils.validateEmail(VALID_EMAIL);
            assertTrue(result.isSuccess());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("邮箱验证 " + ITERATIONS + " 次耗时: " + duration + "ms");
        System.out.println("平均每次验证耗时: " + ((double)duration / ITERATIONS) + "ms");
        
        assertTrue(duration < ITERATIONS, "邮箱验证性能应该满足要求");
    }

    @Test
    @DisplayName("密码验证性能测试")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testPasswordValidationPerformance() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < ITERATIONS; i++) {
            ValidationUtils.ValidationResult result = ValidationUtils.validatePassword(VALID_PASSWORD);
            assertTrue(result.isSuccess());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("密码验证 " + ITERATIONS + " 次耗时: " + duration + "ms");
        System.out.println("平均每次验证耗时: " + ((double)duration / ITERATIONS) + "ms");
        
        assertTrue(duration < ITERATIONS, "密码验证性能应该满足要求");
    }

    @Test
    @DisplayName("用户名验证性能测试")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testUsernameValidationPerformance() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < ITERATIONS; i++) {
            ValidationUtils.ValidationResult result = ValidationUtils.validateUsername(VALID_USERNAME);
            assertTrue(result.isSuccess());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("用户名验证 " + ITERATIONS + " 次耗时: " + duration + "ms");
        System.out.println("平均每次验证耗时: " + ((double)duration / ITERATIONS) + "ms");
        
        assertTrue(duration < ITERATIONS, "用户名验证性能应该满足要求");
    }

    @Test
    @DisplayName("性别验证性能测试")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testGenderValidationPerformance() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < ITERATIONS; i++) {
            ValidationUtils.ValidationResult result = ValidationUtils.validateGender(VALID_GENDER);
            assertTrue(result.isSuccess());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("性别验证 " + ITERATIONS + " 次耗时: " + duration + "ms");
        System.out.println("平均每次验证耗时: " + ((double)duration / ITERATIONS) + "ms");
        
        assertTrue(duration < ITERATIONS, "性别验证性能应该满足要求");
    }

    @RepeatedTest(value = 5, name = "并发验证测试 {currentRepetition}/{totalRepetitions}")
    @DisplayName("并发验证测试")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testConcurrentValidation() throws InterruptedException {
        final int THREAD_COUNT = 10;
        final int ITERATIONS_PER_THREAD = 1000;
        
        Thread[] threads = new Thread[THREAD_COUNT];
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                    // 测试多种验证方法的并发执行
                    ValidationUtils.validatePhoneNumber(VALID_PHONE);
                    ValidationUtils.validateEmail(VALID_EMAIL);
                    ValidationUtils.validatePassword(VALID_PASSWORD);
                    ValidationUtils.validateUsername(VALID_USERNAME);
                    ValidationUtils.validateGender(VALID_GENDER);
                }
            });
            threads[i].start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        int totalValidations = THREAD_COUNT * ITERATIONS_PER_THREAD * 5; // 5种验证方法
        System.out.println("并发执行 " + totalValidations + " 次验证耗时: " + duration + "ms");
        System.out.println("平均每次验证耗时: " + ((double)duration / totalValidations) + "ms");
        
        // 性能断言：并发情况下性能不应该严重下降
        assertTrue(duration < totalValidations / 5, "并发验证性能应该满足要求");
    }

    @Test
    @DisplayName("批量验证性能测试")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBatchValidationPerformance() {
        // 准备测试数据
        String[] phones = {"13812345678", "14712345678", "15012345678", "16612345678", "17712345678"};
        String[] emails = {"user1@test.com", "user2@test.com", "user3@test.com", "user4@test.com", "user5@test.com"};
        String[] passwords = {"Test123", "Pass456", "Secure789", "Strong012", "Valid345"};
        String[] usernames = {"用户1", "user2", "test_user", "张三", "李四"};
        String[] genders = {"男", "女", "未知", "男", "女"};
        
        long startTime = System.currentTimeMillis();
        
        // 批量验证
        for (int i = 0; i < ITERATIONS; i++) {
            for (int j = 0; j < phones.length; j++) {
                ValidationUtils.validatePhoneNumber(phones[j]);
                ValidationUtils.validateEmail(emails[j]);
                ValidationUtils.validatePassword(passwords[j]);
                ValidationUtils.validateUsername(usernames[j]);
                ValidationUtils.validateGender(genders[j]);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        int totalValidations = ITERATIONS * phones.length * 5;
        System.out.println("批量验证 " + totalValidations + " 次耗时: " + duration + "ms");
        System.out.println("平均每次验证耗时: " + ((double)duration / totalValidations) + "ms");
        
        assertTrue(duration < totalValidations / 2, "批量验证性能应该满足要求");
    }

    @Test
    @DisplayName("内存使用测试")
    void testMemoryUsage() {
        // 获取初始内存使用情况
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // 建议进行垃圾回收
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 执行大量验证操作
        for (int i = 0; i < ITERATIONS * 10; i++) {
            ValidationUtils.validatePhoneNumber(VALID_PHONE);
            ValidationUtils.validateEmail(VALID_EMAIL);
            ValidationUtils.validatePassword(VALID_PASSWORD);
            ValidationUtils.validateUsername(VALID_USERNAME);
            ValidationUtils.validateGender(VALID_GENDER);
        }
        
        // 获取执行后内存使用情况
        runtime.gc(); // 建议进行垃圾回收
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        System.out.println("初始内存使用: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("最终内存使用: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("内存增长: " + (memoryIncrease / 1024 / 1024) + " MB");
        
        // 验证内存使用没有异常增长（少于100MB）
        assertTrue(memoryIncrease < 100 * 1024 * 1024, "内存使用增长应该在合理范围内");
    }
} 