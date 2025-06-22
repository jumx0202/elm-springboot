package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.entity.CartItem;
import org.example.service.ICartService;
import org.example.util.ValidationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 * 提供完整的购物车相关API接口，支持接口测试
 */
@RestController
@RequestMapping("/cart")
@Tag(name = "购物车服务接口")
@CrossOrigin(origins = "*")
public class CartController {
    
    @Resource
    private ICartService cartService;
    
    /**
     * 添加商品到购物车
     * 支持各种边界值和异常情况测试
     */
    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车")
    public ResponseEntity<Map<String, Object>> addToCart(
            @Valid @RequestBody CartItem cartItem,
            @Parameter(description = "用户手机号") @RequestHeader(value = "User-Phone", required = false) String userPhone) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 参数验证 - 支持接口测试的异常情况
            if (cartItem == null) {
                response.put("code", 400);
                response.put("message", "请求参数不能为空");
                response.put("data", null);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // 2. 如果请求头中有用户手机号，优先使用
            if (userPhone != null && !userPhone.trim().isEmpty()) {
                cartItem.setUserPhone(userPhone);
            }
            
            // 3. 调用服务层方法
            Integer result = cartService.addToCart(cartItem);
            
            // 4. 根据结果返回不同的响应 - 支持等价类划分测试
            switch (result) {
                case 0:
                    response.put("code", 200);
                    response.put("message", "添加成功");
                    response.put("data", cartItem);
                    return ResponseEntity.ok(response);
                    
                case 1:
                    response.put("code", 400);
                    response.put("message", "商品信息无效");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    
                case 2:
                    response.put("code", 400);
                    response.put("message", "用户信息无效");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    
                case 3:
                    response.put("code", 400);
                    response.put("message", "商品数量超出限制(1-999)");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    
                case 4:
                    response.put("code", 400);
                    response.put("message", "商品价格超出限制(0.01-9999.99)");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    
                case 5:
                    response.put("code", 429);
                    response.put("message", "购物车商品种类已达上限(50种)");
                    return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
                    
                default:
                    response.put("code", 500);
                    response.put("message", "系统异常");
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            // 异常处理 - 支持异常测试
            response.put("code", 500);
            response.put("message", "服务器内部错误: " + e.getMessage());
            response.put("data", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 更新购物车商品数量
     * 支持边界值测试（数量1-999）
     */
    @PutMapping("/update/{itemId}")
    @Operation(summary = "更新购物车商品数量")
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @Parameter(description = "购物车项目ID") @PathVariable Long itemId,
            @Parameter(description = "新数量") @RequestParam Integer quantity) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 参数验证 - 边界值测试
            if (itemId == null || itemId <= 0) {
                response.put("code", 400);
                response.put("message", "商品ID无效");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (quantity == null) {
                response.put("code", 400);
                response.put("message", "数量不能为空");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // 2. 调用服务层方法
            Integer result = cartService.updateQuantity(itemId, quantity);
            
            // 3. 处理不同的返回结果
            switch (result) {
                case 0:
                    response.put("code", 200);
                    response.put("message", "更新成功");
                    return ResponseEntity.ok(response);
                    
                case 1:
                    response.put("code", 400);
                    response.put("message", "商品ID无效");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    
                case 2:
                    response.put("code", 400);
                    response.put("message", "数量无效(1-999)");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    
                case 3:
                    response.put("code", 404);
                    response.put("message", "商品不存在");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                    
                case 4:
                    response.put("code", 410);
                    response.put("message", "商品已失效");
                    return new ResponseEntity<>(response, HttpStatus.GONE);
                    
                case 5:
                    response.put("code", 400);
                    response.put("message", "超出最大数量限制");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    
                case 6:
                    response.put("code", 400);
                    response.put("message", "低于最小数量限制");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    
                default:
                    response.put("code", 500);
                    response.put("message", "系统异常");
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取用户购物车列表
     * 支持分页和筛选功能
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户购物车列表")
    public ResponseEntity<Map<String, Object>> getCartItems(
            @Parameter(description = "用户手机号") @RequestParam String userPhone,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 参数验证
            ValidationUtils.ValidationResult phoneValidation = ValidationUtils.validatePhoneNumber(userPhone);
            if (!phoneValidation.isSuccess()) {
                response.put("code", 400);
                response.put("message", phoneValidation.getMessage());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // 2. 分页参数验证 - 边界值测试
            if (page == null || page < 1) {
                page = 1;
            }
            if (size == null || size < 1 || size > 100) {
                size = 10; // 默认每页10条，最多100条
            }
            
            // 3. 获取购物车数据
            List<CartItem> cartItems = cartService.getCartItems(userPhone);
            Double totalAmount = cartService.calculateTotalAmount(userPhone);
            
            // 4. 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("items", cartItems);
            data.put("totalAmount", totalAmount);
            data.put("itemCount", cartItems.size());
            data.put("page", page);
            data.put("size", size);
            data.put("canCheckout", cartService.validateCartForCheckout(userPhone));
            
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 删除购物车商品
     */
    @DeleteMapping("/remove/{itemId}")
    @Operation(summary = "删除购物车商品")
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @Parameter(description = "购物车项目ID") @PathVariable Long itemId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Boolean result = cartService.removeFromCart(itemId);
            
            if (result) {
                response.put("code", 200);
                response.put("message", "删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "删除失败，商品不存在或ID无效");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车")
    public ResponseEntity<Map<String, Object>> clearCart(
            @Parameter(description = "用户手机号") @RequestParam String userPhone) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Boolean result = cartService.clearCart(userPhone);
            
            if (result) {
                response.put("code", 200);
                response.put("message", "清空成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "清空失败，用户手机号无效");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 验证购物车是否可以结算
     * 支持业务规则测试
     */
    @GetMapping("/validate")
    @Operation(summary = "验证购物车是否可以结算")
    public ResponseEntity<Map<String, Object>> validateCartForCheckout(
            @Parameter(description = "用户手机号") @RequestParam String userPhone) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean canCheckout = cartService.validateCartForCheckout(userPhone);
            Double totalAmount = cartService.calculateTotalAmount(userPhone);
            
            Map<String, Object> data = new HashMap<>();
            data.put("canCheckout", canCheckout);
            data.put("totalAmount", totalAmount);
            
            // 提供详细的验证信息
            if (!canCheckout) {
                if (totalAmount <= 0) {
                    data.put("reason", "购物车为空或总金额为0");
                } else if (totalAmount > ValidationUtils.MAX_ORDER_AMOUNT) {
                    data.put("reason", "订单金额超出限制(" + ValidationUtils.MAX_ORDER_AMOUNT + "元)");
                } else {
                    data.put("reason", "购物车中存在无效商品");
                }
            }
            
            response.put("code", 200);
            response.put("message", "验证完成");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取购物车统计信息
     * 用于系统测试和性能测试
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取购物车统计信息")
    public ResponseEntity<Map<String, Object>> getCartStatistics(
            @Parameter(description = "用户手机号") @RequestParam String userPhone) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 类型转换以调用具体实现类的方法
            if (cartService instanceof org.example.service.impl.CartService) {
                org.example.service.impl.CartService cartServiceImpl = 
                    (org.example.service.impl.CartService) cartService;
                
                String statistics = cartServiceImpl.getCartStatistics(userPhone);
                
                response.put("code", 200);
                response.put("message", "获取成功");
                response.put("data", statistics);
            } else {
                response.put("code", 500);
                response.put("message", "服务不可用");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 批量操作购物车商品
     * 支持复杂的接口测试场景
     */
    @PostMapping("/batch")
    @Operation(summary = "批量操作购物车商品")
    public ResponseEntity<Map<String, Object>> batchOperation(
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String operation = (String) request.get("operation");
            String userPhone = (String) request.get("userPhone");
            
            if (operation == null || userPhone == null) {
                response.put("code", 400);
                response.put("message", "操作类型和用户手机号不能为空");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            switch (operation.toLowerCase()) {
                case "clear":
                    Boolean clearResult = cartService.clearCart(userPhone);
                    response.put("code", clearResult ? 200 : 400);
                    response.put("message", clearResult ? "批量清空成功" : "批量清空失败");
                    break;
                    
                case "validate":
                    Boolean isValid = cartService.validateCartForCheckout(userPhone);
                    response.put("code", 200);
                    response.put("message", "批量验证完成");
                    response.put("data", Map.of("valid", isValid));
                    break;
                    
                default:
                    response.put("code", 400);
                    response.put("message", "不支持的操作类型: " + operation);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 