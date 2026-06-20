export const DEFAULT_PAGE_NUM = 1
export const DEFAULT_PAGE_SIZE = 10
export const PAGE_SIZE_OPTIONS = [5, 10, 20, 50]
export const PAGINATION_LAYOUT = 'total, sizes, prev, pager, next, jumper'

export function applyPageResult(result, target) {
  target.rows.value = result?.records || []
  target.total.value = result?.total || 0
  target.pageNum.value = result?.pageNum || DEFAULT_PAGE_NUM
  target.pageSize.value = result?.pageSize || DEFAULT_PAGE_SIZE
}

export function applyReactivePageResult(result, target) {
  target.rows.value = result?.records || []
  target.pagination.total = result?.total || 0
  target.pagination.pageNum = result?.pageNum || DEFAULT_PAGE_NUM
  target.pagination.pageSize = result?.pageSize || DEFAULT_PAGE_SIZE
}
