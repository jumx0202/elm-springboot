<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import TabBar from '@/components/TabBar.vue'

interface Order {
  id: number
  businessID: number
  state: number
  price: number
  orderList: string
  userPhone: string
  createdAt: string
}

// 添加商家信息接口
interface Business {
  id: number
  businessName: string
  address: string
  phone: string
  rating: string
  imgLogo: string
  description: string
  discounts: string
  minOrder: string
  delivery: string
  sales: string
  notice: string
  type: string
}

// 扩展订单显示接口
interface OrderDisplay extends Order {
  businessName: string
  imgLogo: string
  orderItems: OrderItem[]
  amount: number
}

interface OrderItem {
  id: number
  name: string
  img: string
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const orders = ref<Order[]>([])
const activeTab = ref('all')

const toShop = (id: number) => {
  router.push(`/shop/${id}`)
}

const getOrderItems = async (orderList: string) => {
  const itemIds = orderList.split('-')
  const items = await Promise.all(
    itemIds.map(async (id) => {
      try {
        const response = await axios.post('http://localhost:8080/api/food/getFoodById', {
          'ID': Number(id)
        })
        return response.data
      } catch (error) {
        console.error(`Failed to fetch food item ${id}:`, error)
        return null
      }
    })
  )
  return items.filter(item => item !== null)
}

const getBusinessInfo = async (businessID: number) => {
  try {
    console.log('Fetching business info for ID:', businessID)
    const response = await axios.post("http://localhost:8080/api/business/getBusinessById", {
      'ID': businessID
    })
    console.log('Full business response:', response)
    console.log('Business response data:', response.data)
    console.log('Business response data type:', typeof response.data)
    if (response.data && response.data.data) {
      console.log('Business data from response:', response.data.data)
      return response.data.data
    } else if (response.data) {
      console.log('Direct business data:', response.data)
      return response.data
    }
    throw new Error('Invalid business data structure')
  } catch (error) {
    console.error(`Failed to fetch business info for ID ${businessID}:`, error)
    return null
  }
}

onMounted(async () => {
  const type = route.query.type as string
  if (type) {
    activeTab.value = type
  }

  try {
    if (!userStore.userInfo) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    const res = await axios.post("http://localhost:8080/api/order/getAllUserOrder", {
      'userPhone': userStore.userInfo.phoneNumber
    })

    console.log('Orders response:', res.data)

    const ordersData = res.data
    console.log('Filtered orders data:', ordersData)

    const filteredOrders = ordersData.filter(order => {
      switch (activeTab.value) {
        case 'ongoing':
          return order.state === 0
        case 'pending':
          return order.state === 1
        case 'refund':
          return order.state === 2
        default:
          return true
      }
    })
    console.log('After filtering:', filteredOrders)

    const detailedOrders = await Promise.all(
      filteredOrders.map(async (order) => {
        try {
          if (!order.id) {
            console.error('Order ID is missing:', order)
            return order
          }
          console.log('Processing order:', order)
          const businessRes = await getBusinessInfo(order.businessID)
          console.log('Business info received:', businessRes)
          const orderItems = await getOrderItems(order.orderList)
          console.log('Order items received:', orderItems)
          
          const result = { 
            ...order, 
            businessName: businessRes?.businessName || '未知商家',
            imgLogo: businessRes?.imgLogo || '/img/default-shop.png',
            orderItems,
            amount: order.orderList.split('-').length
          }
          console.log('Final order object:', result)
          return result
        } catch (error) {
          console.error(`Failed to fetch details for order ${order.id}`, error)
          return order
        }
      })
    )

    console.log('Final detailed orders:', detailedOrders)
    orders.value = detailedOrders
  } catch (error) {
    console.error("Failed to fetch user orders", error)
    ElMessage.error('获取订单失败')
  }
})
</script>

<template>
  <div class="wrapper3">
    <div class="header">
      <div class="selector">
        <div class="item1">
          全部
          <div class="selected"></div>
        </div>
        <div class="item1">
          进行中
          <div class="not-selected"></div>
        </div>
        <div class="item1">
          待评价
          <div class="not-selected"></div>
        </div>
        <div class="item1">
          退款
          <div class="not-selected"></div>
        </div>
      </div>
    </div>

    <div class="order-list">
      <div v-for="(item, index) in orders" :key="index" class="order-card">
        <div class="shop-header">
          <img :src="item.imgLogo || '/img/default-shop.png'" alt="shop image" class="shop-img"/>
          <div class="shop-info">
            <div class="business-name">{{ item.businessName || '未知商家' }} ></div>
            <div class="order-status">{{ item.state === 1 ? '已支付' : '未支付' }}</div>
          </div>
        </div>

        <div class="order-items">
          <div class="items-container">
            <template v-if="item.orderItems?.length === 1">
              <img :src="item.orderItems[0].img" class="food-img" />
              <span class="food-name">{{ item.orderItems[0].name }}</span>
            </template>
            <template v-else>
              <div class="multi-items">
                <img 
                  v-for="(food, idx) in item.orderItems" 
                  :key="idx"
                  :src="food.img" 
                  class="food-img"
                />
              </div>
            </template>
          </div>
          <div class="order-info">
            <div class="price">¥{{ item.price.toFixed(2) }}</div>
            <div class="count">共{{ item.amount }}件</div>
          </div>
        </div>

        <div class="order-time">{{ new Date(item.createdAt).toLocaleString() }}</div>
        <div class="action-buttons">
          <button class="similar-shop">相似商家</button>
          <button class="reorder-btn" @click="toShop(item.businessID)">再来一单</button>
        </div>
      </div>
    </div>

    <div class="tip">仅显示最近一年的订单</div>
    <TabBar />
  </div>
</template>

<style scoped>
.wrapper3 {
  min-height: 100vh;
  width: 100%;
  background: #F5F5F5;
  padding-bottom: 60px;
}

.header {
  background: white;
  padding: 15px;
}

.selector {
  display: flex;
  justify-content: space-around;
  border-bottom: 1px solid #f5f5f5;
}

.item1 {
  position: relative;
  padding: 12px 0;
  font-size: 14px;
  color: #333;
}

.selected {
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: #0095ff;
}

.not-selected {
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: transparent;
}

.order-card {
  background: white;
  margin-bottom: 10px;
  padding: 16px;
}

.shop-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.shop-img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 12px;
}

.shop-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.business-name {
  font-size: 16px;
  font-weight: 500;
}

.order-status {
  color: #666;
  font-size: 14px;
}

.order-details {
  margin: 12px 0;
}

.price-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.count {
  color: #999;
}

.order-time {
  color: #999;
  font-size: 12px;
  margin-bottom: 12px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.similar-shop,
.reorder-btn {
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 14px;
  cursor: pointer;
}

.similar-shop {
  background: white;
  border: 1px solid #ddd;
  color: #666;
}

.reorder-btn {
  background: #0095ff;
  color: white;
  border: none;
}

.tip {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 15px 0;
}

.order-items {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 12px 0;
  padding: 12px 0;
  border-top: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
}

.items-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.food-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.food-name {
  font-size: 14px;
  color: #333;
}

.multi-items {
  display: flex;
  gap: 8px;
}

.order-info {
  text-align: right;
}

.price {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.count {
  font-size: 12px;
  color: #999;
}
</style> 