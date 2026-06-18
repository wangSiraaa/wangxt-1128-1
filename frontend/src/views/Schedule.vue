<template>
  <div class="page-card">
    <div class="page-header">
      <span class="page-title">排期管理</span>
      <div>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增排期</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-select v-model="searchForm.screenId" placeholder="选择广告屏" clearable style="width: 180px">
        <el-option v-for="s in screenList" :key="s.id" :label="s.screenName" :value="s.id" />
      </el-select>
      <el-date-picker
        v-model="searchForm.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 280px"
      />
      <el-select v-model="searchForm.scheduleStatus" placeholder="排期状态" clearable style="width: 140px">
        <el-option label="待刊播" :value="1" />
        <el-option label="刊播中" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
      <el-select v-model="searchForm.proofStatus" placeholder="证明状态" clearable style="width: 140px">
        <el-option label="未回传" :value="0" />
        <el-option label="已回传" :value="1" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="scheduleCode" label="排期编号" width="180" />
      <el-table-column prop="screenName" label="广告屏" />
      <el-table-column prop="materialName" label="素材名称" />
      <el-table-column prop="customerName" label="客户" />
      <el-table-column label="播放日期" width="120">
        <template #default="{ row }">
          {{ formatDate(row.playDate) }}
        </template>
      </el-table-column>
      <el-table-column label="时段" width="150">
        <template #default="{ row }">
          {{ row.startTime }} - {{ row.endTime }}
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="时长(秒)" width="90" />
      <el-table-column label="排期状态" width="100">
        <template #default="{ row }">
          <el-tag :class="getScheduleTagClass(row.scheduleStatus)" size="small">
            {{ getScheduleStatusText(row.scheduleStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="证明状态" width="100">
        <template #default="{ row }">
          <el-tag :class="row.proofStatus === 1 ? 'tag-success' : 'tag-warning'" size="small">
            {{ row.proofStatus === 1 ? '已回传' : '未回传' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button
              type="warning"
              link
              size="small"
              @click="openEditDialog(row)"
              :disabled="row.scheduleStatus === 4"
            >编辑</el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleCancel(row)"
              :disabled="row.scheduleStatus === 4 || row.proofStatus === 1"
            >取消</el-button>
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

    <el-dialog v-model="createDialogVisible" :title="isEdit ? '编辑排期' : '新增排期'" width="600px">
      <el-form :model="scheduleForm" :rules="scheduleRules" ref="scheduleFormRef" label-width="100px">
        <el-form-item label="广告屏" prop="screenId">
          <el-select v-model="scheduleForm.screenId" placeholder="请选择广告屏" style="width: 100%">
            <el-option v-for="s in screenList" :key="s.id" :label="s.screenName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="素材" prop="materialId">
          <el-select v-model="scheduleForm.materialId" placeholder="请选择素材" style="width: 100%" filterable>
            <el-option
              v-for="m in auditedMaterials"
              :key="m.id"
              :label="`${m.materialName} - ${m.customerName}`"
              :value="m.id"
            />
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 4px">仅显示审核通过的素材</div>
        </el-form-item>
        <el-form-item label="播放日期" prop="playDate">
          <el-date-picker
            v-model="scheduleForm.playDate"
            type="date"
            placeholder="选择播放日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="scheduleForm.startTime"
            placeholder="选择开始时间"
            value-format="HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="scheduleForm.endTime"
            placeholder="选择结束时间"
            value-format="HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="播放时长" prop="duration">
          <el-input-number v-model="scheduleForm.duration" :min="1" :max="86400" />
          <span style="margin-left: 8px; color: #909399">秒</span>
          <span v-if="isEdit && currentRow?.proofStatus === 1" style="margin-left: 12px; color: #f56c6c; font-size: 12px">
            已回传证明，时长不可修改
          </span>
        </el-form-item>
        <el-form-item label="播放顺序">
          <el-input-number v-model="scheduleForm.playOrder" :min="0" />
        </el-form-item>
        <el-form-item label="创建人" prop="createBy" v-if="!isEdit">
          <el-input v-model="scheduleForm.createBy" placeholder="请输入操作人姓名" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="scheduleForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="checkConflictAndSave">
          <el-icon><Search /></el-icon>
          检查冲突并保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="排期详情" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="排期编号">{{ detail.scheduleCode }}</el-descriptions-item>
        <el-descriptions-item label="广告屏">{{ detail.screenName }}</el-descriptions-item>
        <el-descriptions-item label="素材名称">{{ detail.materialName }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detail.customerName }}</el-descriptions-item>
        <el-descriptions-item label="播放日期">{{ formatDate(detail.playDate) }}</el-descriptions-item>
        <el-descriptions-item label="时段">{{ detail.startTime }} - {{ detail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="时长">{{ detail.duration }} 秒</el-descriptions-item>
        <el-descriptions-item label="播放顺序">{{ detail.playOrder }}</el-descriptions-item>
        <el-descriptions-item label="排期状态">
          <el-tag :class="getScheduleTagClass(detail.scheduleStatus)">{{ getScheduleStatusText(detail.scheduleStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="证明状态">
          <el-tag :class="detail.proofStatus === 1 ? 'tag-success' : 'tag-warning'">
            {{ detail.proofStatus === 1 ? '已回传' : '未回传' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  schedulePage, scheduleCreate, scheduleUpdate, scheduleCancel, scheduleCheckConflict,
  screenListEnabled, materialListAudited
} from '@/api'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const screenList = ref([])
const auditedMaterials = ref([])
const searchForm = reactive({ screenId: null, dateRange: [], scheduleStatus: null, proofStatus: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const createDialogVisible = ref(false)
const isEdit = ref(false)
const currentRow = ref(null)
const scheduleFormRef = ref(null)
const scheduleForm = reactive({
  id: null, screenId: null, materialId: null, playDate: null,
  startTime: null, endTime: null, duration: 15, playOrder: 0,
  remark: '', createBy: '', updateBy: ''
})
const scheduleRules = {
  screenId: [{ required: true, message: '请选择广告屏', trigger: 'change' }],
  materialId: [{ required: true, message: '请选择素材', trigger: 'change' }],
  playDate: [{ required: true, message: '请选择播放日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  duration: [{ required: true, message: '请输入播放时长', trigger: 'blur' }],
  createBy: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

const detailDialogVisible = ref(false)
const detail = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      screenId: searchForm.screenId,
      scheduleStatus: searchForm.scheduleStatus,
      proofStatus: searchForm.proofStatus
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await schedulePage(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const loadBaseData = async () => {
  try {
    const [screens, materials] = await Promise.all([screenListEnabled(), materialListAudited()])
    screenList.value = screens || []
    auditedMaterials.value = materials || []
  } catch (e) {}
}

const resetSearch = () => {
  searchForm.screenId = null
  searchForm.dateRange = []
  searchForm.scheduleStatus = null
  searchForm.proofStatus = null
  pagination.pageNum = 1
  loadData()
}

const openCreateDialog = () => {
  isEdit.value = false
  currentRow.value = null
  Object.assign(scheduleForm, {
    id: null, screenId: null, materialId: null, playDate: null,
    startTime: null, endTime: null, duration: 15, playOrder: 0,
    remark: '', createBy: '', updateBy: ''
  })
  createDialogVisible.value = true
}

const openEditDialog = row => {
  isEdit.value = true
  currentRow.value = row
  Object.assign(scheduleForm, {
    id: row.id, screenId: row.screenId, materialId: row.materialId,
    playDate: row.playDate, startTime: row.startTime, endTime: row.endTime,
    duration: row.duration, playOrder: row.playOrder, remark: row.remark,
    createBy: '', updateBy: 'editor'
  })
  createDialogVisible.value = true
}

const checkConflictAndSave = async () => {
  await scheduleFormRef.value.validate()
  try {
    const hasConflict = await scheduleCheckConflict({
      screenId: scheduleForm.screenId,
      playDate: scheduleForm.playDate,
      startTime: scheduleForm.startTime,
      endTime: scheduleForm.endTime,
      excludeId: isEdit.value ? scheduleForm.id : null
    })
    if (hasConflict) {
      ElMessage.error('该广告屏在此时段已有排期，请调整时间')
      return
    }
    if (isEdit.value) {
      await scheduleUpdate(scheduleForm)
      ElMessage.success('排期修改成功')
    } else {
      await scheduleCreate(scheduleForm)
      ElMessage.success('排期创建成功')
    }
    createDialogVisible.value = false
    loadData()
  } catch (e) {}
}

const viewDetail = row => {
  detail.value = row
  detailDialogVisible.value = true
}

const handleCancel = async row => {
  try {
    await ElMessageBox.confirm('确定取消该排期吗？取消后不可恢复。', '提示', { type: 'warning' })
    await scheduleCancel(row.id, 'operator')
    ElMessage.success('排期已取消')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const formatDate = d => d ? dayjs(d).format('YYYY-MM-DD') : '-'
const getScheduleStatusText = s => ({ 1: '待刊播', 2: '刊播中', 3: '已完成', 4: '已取消' }[s] || '-')
const getScheduleTagClass = s => ({ 1: 'tag-warning', 2: 'tag-primary', 3: 'tag-success', 4: 'tag-info' }[s] || 'tag-info')

onMounted(async () => {
  await loadBaseData()
  loadData()
})
</script>
