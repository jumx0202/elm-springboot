<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const shopList = ref([])

const getShopList = async () => {
  try {
    const response = await axios.post('http://localhost:8080/api/business/getAll')
    shopList.value = response.data
  } catch (error) {
    console.error('获取商家列表失败:', error)
  }
}

const toShopInfo = (id: number) => {
  router.push(`/shop/${id}`)
}

onMounted(() => {
  getShopList()
})
</script>

<template>
  <div class="shop-list">
    <div class="header">
      <h1>商家列表</h1>
    </div>
    
    <div class="shop-items">
      <div 
        v-for="shop in shopList" 
        :key="shop.id" 
        class="shop-item"
        @click="toShopInfo(shop.id)"
      >
        <img :src="shop.imgLogo" :alt="shop.businessName" class="shop-logo">
        <div class="shop-info">
          <h2>{{ shop.businessName }}</h2>
          <div class="shop-rating">
            <span>{{ shop.rating }}</span>
            <span>月售{{ shop.sales }}</span>
          </div>
          <div class="shop-delivery">
            <span>{{ shop.distance }}</span>
            <span>{{ shop.delivery }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.shop-list {
  padding: 16px;
  background: #f5f5f5;
}

.header {
  margin-bottom: 16px;
}

.shop-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.shop-item {
  display: flex;
  padding: 16px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
}

.shop-logo {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 16px;
}

.shop-info {
  flex: 1;
}

.shop-rating {
  margin: 8px 0;
  color: #666;
}

.shop-delivery {
  color: #999;
  font-size: 14px;
}
</style> 