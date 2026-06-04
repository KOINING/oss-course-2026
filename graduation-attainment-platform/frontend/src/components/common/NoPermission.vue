<script setup>
import { computed } from 'vue'

const props = defineProps({
  requiredRole: {
    type: String,
    default: '',
  },
})

const subTitle = computed(() => {
  if (props.requiredRole) {
    return `需要「${props.requiredRole}」角色才能访问此页面`
  }
  return '当前账号无权访问此页面，请联系管理员'
})
</script>

<template>
  <div class="no-permission">
    <el-result
      icon="warning"
      title="无访问权限"
      :sub-title="subTitle"
    />
  </div>
</template>

<style scoped>
.no-permission {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
}
</style>
