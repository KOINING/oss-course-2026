<script setup>
defineProps({
  description: {
    type: String,
    default: '暂无数据',
  },
  image: {
    type: String,
    default: undefined,
  },
  imageSize: {
    type: Number,
    default: 120,
  },
})
</script>

<template>
  <div class="empty-state">
    <el-empty
      :image="image"
      :image-size="imageSize"
      :description="description"
    />
    <div v-if="$slots.actions" class="empty-state__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
}

.empty-state__actions {
  margin-top: 8px;
}
</style>
