<script setup>
defineProps({
  message: {
    type: String,
    default: '加载失败，请稍后重试',
  },
  showRetry: {
    type: Boolean,
    default: true,
  },
})

defineEmits(['retry'])
</script>

<template>
  <div class="error-state">
    <el-result
      icon="error"
      title="加载失败"
      :sub-title="message"
    >
      <template v-if="showRetry" #extra>
        <el-button type="primary" @click="$emit('retry')">
          重新加载
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.error-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
}
</style>
