<template>
  <div class="page-card">
    <div class="page-header">
      <span class="page-title">刊播证明</span>
      <el-button type="primary" :icon="Plus" @click="openSubmitDialog">回传证明</el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="searchForm.screenId" placeholder="选择广告屏" clearable style="width: 180px">
        <el-option v-for="s in screenList" :key="s.id" :label="s.screenName" :value="s.id" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="proofCode" label="证明编号" width="180" />
      <el-table-column prop="scheduleId" label="排期ID" width="100" />
      <el-table-column label="证明类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ getProofTypeText(row.proofType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileName" label="文件名称" show-overflow-tooltip />
      <el-table-column label="实际播放时长" width="120">
        <template #default="{ row }">
          {{ row.actualDuration ? row.actualDuration + ' 秒' : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="submitBy" label="回传人" width="100" />
      <el-table-column label="回传时间" width="170">
        <template #default="{ row }">
          {{ formatDateTime(row.submitTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
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

    <el-dialog v-model="submitDialogVisible" title="回传刊播证明" width="560px">
      <el-form :model="proofForm" :rules="proofRules" ref="proofFormRef" label-width="110px">
        <el-form-item label="关联排期" prop="scheduleId">
          <el-select v-model="proofForm.scheduleId" placeholder="请选择排期" style="width: 100%" filterable>
            <el-option
              v-for="s in pendingProofSchedules"
              :key="s.id"
              :label="`${s.scheduleCode} - ${s.screenName} - ${formatDate(s.playDate)}`"
              :value="s.id"
            />
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 4px">仅显示未回传证明的排期</div>
        </el-form-item>
        <el-form-item label="证明类型" prop="proofType">
          <el-select v-model="proofForm.proofType" placeholder="请选择证明类型" style="width: 100%">
            <el-option label="截图" :value="1" />
            <el-option label="视频" :value="2" />
            <el-option label="日志" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件URL" prop="fileUrl">
          <el-input v-model="proofForm.fileUrl" placeholder="请输入证明文件URL" />
        </el-form-item>
        <el-form-item label="文件名称">
          <el-input v-model="proofForm.fileName" placeholder="请输入文件名称" />
        </el-form-item>
        <el-form-item label="实际开始时间">
          <el-date-picker
            v-model="proofForm.actualStartTime"
            type="datetime"
            placeholder="选择实际开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="实际结束时间">
          <el-date-picker
            v-model="proofForm.actualEndTime"
            type="datetime"
            placeholder="选择实际结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="实际时长">
          <el-input-number v-model="proofForm.actualDuration" :min="1" :max="86400" />
          <span style="margin-left: 8px; color: #909399">秒</span>
        </el-form-item>
        <el-form-item label="回传人" prop="submitBy">
          <el-input v-model="proofForm.submitBy" placeholder="请输入回传人姓名" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="proofForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProof">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="刊播证明详情" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="证明编号">{{ detail.proofCode }}</el-descriptions-item>
        <el-descriptions-item label="排期ID">{{ detail.scheduleId }}</el-descriptions-item>
        <el-descriptions-item label="证明类型">{{ getProofTypeText(detail.proofType) }}</el-descriptions-item>
        <el-descriptions-item label="文件名称">{{ detail.fileName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际开始时间">{{ formatDateTime(detail.actualStartTime) }}</el-descriptions-item>
        <el-descriptions-item label="实际结束时间">{{ formatDateTime(detail.actualEndTime) }}</el-descriptions-item>
        <el-descriptions-item label="实际时长">{{ detail.actualDuration ? detail.actualDuration + ' 秒' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="回传人">{{ detail.submitBy }}</el-descriptions-item>
        <el-descriptions-item label="回传时间" :span="2">{{ formatDateTime(detail.submitTime) }}</el-descriptions-item>
        <el-descriptions-item label="文件URL" :span="2">{{ detail.fileUrl }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { proofPage, proofSubmit, schedulePage, screenListEnabled } from '@/api'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const screenList = ref([])
const pendingProofSchedules = ref([])
const searchForm = reactive({ screenId: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const submitDialogVisible = ref(false)
const proofFormRef = ref(null)
const proofForm = reactive({
  scheduleId: null, proofType: 1, fileUrl: '', fileName: '',
  actualStartTime: null, actualEndTime: null, actualDuration: null,
  remark: '', submitBy: ''
})
const proofRules = {
  scheduleId: [{ required: true, message: '请选择排期', trigger: 'change' }],
  proofType: [{ required: true, message: '请选择证明类型', trigger: 'change' }],
  fileUrl: [{ required: true, message: '请输入文件URL', trigger: 'blur' }],
  submitBy: [{ required: true, message: '请输入回传人', trigger: 'blur' }]
}

const detailDialogVisible = ref(false)
const detail = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await proofPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      screenId: searchForm.screenId
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const loadBaseData = async () => {
  try {
    const [screens, scheduleRes] = await Promise.all([
      screenListEnabled(),
      schedulePage({ pageNum: 1, pageSize: 100, proofStatus: 0 })
    ])
    screenList.value = screens || []
    pendingProofSchedules.value = scheduleRes.records || []
  } catch (e) {}
}

const resetSearch = () => {
  searchForm.screenId = null
  pagination.pageNum = 1
  loadData()
}

const openSubmitDialog = () => {
  Object.assign(proofForm, {
    scheduleId: null, proofType: 1, fileUrl: '', fileName: '',
    actualStartTime: null, actualEndTime: null, actualDuration: null,
    remark: '', submitBy: ''
  })
  submitDialogVisible.value = true
}

const submitProof = async () => {
  await proofFormRef.value.validate()
  try {
    await proofSubmit(proofForm)
    ElMessage.success('刊播证明回传成功')
    submitDialogVisible.value = false
    loadData()
    loadBaseData()
  } catch (e) {}
}

const viewDetail = row => {
  detail.value = row
  detailDialogVisible.value = true
}

const getProofTypeText = t => ({ 1: '截图', 2: '视频', 3: '日志' }[t] || '-')
const formatDate = d => d ? dayjs(d).format('YYYY-MM-DD') : '-'
const formatDateTime = d => d ? dayjs(d).format('YYYY-MM-DD HH:mm:ss') : '-'

onMounted(async () => {
  await loadBaseData()
  loadData()
})
</script>
