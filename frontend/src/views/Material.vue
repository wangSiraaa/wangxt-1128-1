<template>
  <div class="page-card">
    <div class="page-header">
      <span class="page-title">素材管理</span>
      <el-button type="primary" :icon="Plus" @click="openSubmitDialog">提交素材</el-button>
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
      <el-table-column prop="duration" label="时长(秒)" width="100" />
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <el-tag :class="getAuditTagClass(row.auditStatus)" size="small">
            {{ getAuditStatusText(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditor" label="审核人" width="100" />
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
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
              :disabled="row.auditStatus === 1"
            >删除</el-button>
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

    <el-dialog v-model="submitDialogVisible" title="提交素材" width="560px">
      <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef" label-width="100px">
        <el-form-item label="素材名称" prop="materialName">
          <el-input v-model="submitForm.materialName" placeholder="请输入素材名称" />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="submitForm.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="素材类型" prop="materialType">
          <el-select v-model="submitForm.materialType" placeholder="请选择素材类型" style="width: 100%">
            <el-option label="图片" :value="1" />
            <el-option label="视频" :value="2" />
            <el-option label="文字" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="播放时长" prop="duration">
          <el-input-number v-model="submitForm.duration" :min="1" :max="86400" />
          <span style="margin-left: 8px; color: #909399">秒</span>
        </el-form-item>
        <el-form-item label="素材文件" prop="fileUrl">
          <el-input v-model="submitForm.fileUrl" placeholder="请输入文件URL或上传" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="submitForm.description" type="textarea" :rows="3" placeholder="请输入素材描述" />
        </el-form-item>
        <el-form-item label="提交人" prop="createBy">
          <el-input v-model="submitForm.createBy" placeholder="请输入提交人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMaterial">提交</el-button>
      </template>
    </el-dialog>

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
        <el-descriptions-item label="审核意见" v-if="detail.auditRemark">{{ detail.auditRemark }}</el-descriptions-item>
        <el-descriptions-item label="审核人" v-if="detail.auditor">{{ detail.auditor }}</el-descriptions-item>
        <el-descriptions-item label="文件地址">{{ detail.fileUrl }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ detail.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { materialPage, materialSubmit, materialDelete } from '@/api'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ auditStatus: null, customerName: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const submitDialogVisible = ref(false)
const submitFormRef = ref(null)
const submitForm = reactive({
  materialName: '', customerName: '', materialType: 1, duration: 15, fileUrl: '', description: '', createBy: ''
})
const submitRules = {
  materialName: [{ required: true, message: '请输入素材名称', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  materialType: [{ required: true, message: '请选择素材类型', trigger: 'change' }],
  duration: [{ required: true, message: '请输入播放时长', trigger: 'blur' }],
  fileUrl: [{ required: true, message: '请输入文件地址', trigger: 'blur' }],
  createBy: [{ required: true, message: '请输入提交人', trigger: 'blur' }]
}

const detailDialogVisible = ref(false)
const detail = ref({})

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
  searchForm.auditStatus = null
  searchForm.customerName = ''
  pagination.pageNum = 1
  loadData()
}

const openSubmitDialog = () => {
  Object.assign(submitForm, { materialName: '', customerName: '', materialType: 1, duration: 15, fileUrl: '', description: '', createBy: '' })
  submitDialogVisible.value = true
}

const submitMaterial = async () => {
  await submitFormRef.value.validate()
  try {
    await materialSubmit(submitForm)
    ElMessage.success('素材提交成功，等待审核')
    submitDialogVisible.value = false
    loadData()
  } catch (e) {}
}

const viewDetail = row => {
  detail.value = row
  detailDialogVisible.value = true
}

const handleDelete = async row => {
  try {
    await ElMessageBox.confirm('确定删除该素材吗？', '提示', { type: 'warning' })
    await materialDelete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const getMaterialTypeText = t => ({ 1: '图片', 2: '视频', 3: '文字' }[t] || '-')
const getAuditStatusText = s => ({ 0: '待审核', 1: '审核通过', 2: '审核未通过' }[s] || '-')
const getAuditTagClass = s => ({ 0: 'tag-warning', 1: 'tag-success', 2: 'tag-danger' }[s] || 'tag-info')
const formatDateTime = d => d ? dayjs(d).format('YYYY-MM-DD HH:mm:ss') : '-'

onMounted(loadData)
</script>
