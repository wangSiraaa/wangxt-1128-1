<template>
  <div class="page-card">
    <div class="page-header">
      <span class="page-title">内容审核</span>
    </div>

    <div class="search-bar">
      <el-select v-model="searchForm.auditStatus" placeholder="审核状态" clearable style="width: 160px">
        <el-option label="待审核" :value="0" />
        <el-option label="审核通过" :value="1" />
        <el-option label="审核未通过" :value="2" />
      </el-select>
      <el-input v-model="searchForm.customerName" placeholder="客户名称" clearable style="width: 200px" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="materialCode" label="素材编号" width="180" />
      <el-table-column prop="materialName" label="素材名称" />
      <el-table-column prop="customerName" label="客户名称" />
      <el-table-column label="素材类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ getMaterialTypeText(row.materialType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <el-tag :class="getAuditTagClass(row.auditStatus)" size="small">
            {{ getAuditStatusText(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="170">
        <template #default="{ row }">
          {{ formatDateTime(row.submitTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button type="primary" link size="small" @click="viewDetail(row)">查看</el-button>
            <el-button
              type="success"
              link
              size="small"
              @click="openAuditDialog(row, 1)"
              :disabled="row.auditStatus !== 0"
            >通过</el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="openAuditDialog(row, 2)"
              :disabled="row.auditStatus !== 0"
            >驳回</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 50]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 16px; justify-content: flex-end"
      @size-change="loadData"
      @current-change="loadData"
    />

    <el-dialog v-model="detailDialogVisible" title="素材详情" width="560px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="素材编号">{{ detail.materialCode }}</el-descriptions-item>
        <el-descriptions-item label="素材名称">{{ detail.materialName }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ detail.customerName }}</el-descriptions-item>
        <el-descriptions-item label="素材类型">{{ getMaterialTypeText(detail.materialType) }}</el-descriptions-item>
        <el-descriptions-item label="播放时长">{{ detail.duration }} 秒</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :class="getAuditTagClass(detail.auditStatus)">{{ getAuditStatusText(detail.auditStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="文件地址">{{ detail.fileUrl }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ detail.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" v-if="detail.auditRemark">{{ detail.auditRemark }}</el-descriptions-item>
        <el-descriptions-item label="审核人" v-if="detail.auditor">{{ detail.auditor }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="auditDialogVisible" :title="auditForm.auditStatus === 1 ? '审核通过' : '审核驳回'" width="500px">
      <el-form :model="auditForm" :rules="auditRules" ref="auditFormRef" label-width="80px">
        <el-form-item label="审核人" prop="auditor">
          <el-input v-model="auditForm.auditor" placeholder="请输入审核人姓名" />
        </el-form-item>
        <el-form-item label="审核意见" prop="auditRemark">
          <el-input
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="4"
            :placeholder="auditForm.auditStatus === 1 ? '请输入审核意见（可选）' : '请输入驳回原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button :type="auditForm.auditStatus === 1 ? 'success' : 'danger'" @click="doAudit">
          确认{{ auditForm.auditStatus === 1 ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { materialPage, materialAudit } from '@/api'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ auditStatus: 0, customerName: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const detailDialogVisible = ref(false)
const detail = ref({})

const auditDialogVisible = ref(false)
const auditFormRef = ref(null)
const auditForm = reactive({ materialId: null, auditStatus: 1, auditRemark: '', auditor: '' })
const auditRules = {
  auditor: [{ required: true, message: '请输入审核人', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await materialPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.auditStatus = 0
  searchForm.customerName = ''
  pagination.pageNum = 1
  loadData()
}

const viewDetail = row => {
  detail.value = row
  detailDialogVisible.value = true
}

const openAuditDialog = (row, status) => {
  auditForm.materialId = row.id
  auditForm.auditStatus = status
  auditForm.auditRemark = ''
  auditForm.auditor = ''
  auditDialogVisible.value = true
}

const doAudit = async () => {
  await auditFormRef.value.validate()
  try {
    await materialAudit(auditForm)
    ElMessage.success(auditForm.auditStatus === 1 ? '审核通过' : '已驳回')
    auditDialogVisible.value = false
    loadData()
  } catch (e) {}
}

const getMaterialTypeText = t => ({ 1: '图片', 2: '视频', 3: '文字' }[t] || '-')
const getAuditStatusText = s => ({ 0: '待审核', 1: '审核通过', 2: '审核未通过' }[s] || '-')
const getAuditTagClass = s => ({ 0: 'tag-warning', 1: 'tag-success', 2: 'tag-danger' }[s] || 'tag-info')
const formatDateTime = d => d ? dayjs(d).format('YYYY-MM-DD HH:mm:ss') : '-'

onMounted(loadData)
</script>
