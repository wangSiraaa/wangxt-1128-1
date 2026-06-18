<template>
  <div class="page-card">
    <div class="page-header">
      <span class="page-title">广告屏管理</span>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增广告屏</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="screenCode" label="屏幕编码" width="140" />
      <el-table-column prop="screenName" label="屏幕名称" />
      <el-table-column prop="location" label="安装位置" />
      <el-table-column prop="resolution" label="分辨率" width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :class="row.status === 1 ? 'tag-success' : 'tag-info'" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑广告屏' : '新增广告屏'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="屏幕编码" prop="screenCode">
          <el-input v-model="form.screenCode" placeholder="请输入屏幕编码" />
        </el-form-item>
        <el-form-item label="屏幕名称" prop="screenName">
          <el-input v-model="form.screenName" placeholder="请输入屏幕名称" />
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="form.location" placeholder="请输入安装位置" />
        </el-form-item>
        <el-form-item label="分辨率">
          <el-input v-model="form.resolution" placeholder="如：1920x1080" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { screenList, screenSave, screenUpdate, screenDelete } from '@/api'

const loading = ref(false)
const tableData = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, screenCode: '', screenName: '', location: '',
  resolution: '', status: 1, description: '', createBy: ''
})
const rules = {
  screenCode: [{ required: true, message: '请输入屏幕编码', trigger: 'blur' }],
  screenName: [{ required: true, message: '请输入屏幕名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await screenList()
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null, screenCode: '', screenName: '', location: '',
    resolution: '', status: 1, description: '', createBy: ''
  })
  dialogVisible.value = true
}

const openEditDialog = row => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await screenUpdate(form)
      ElMessage.success('修改成功')
    } else {
      await screenSave(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {}
}

const handleDelete = async row => {
  try {
    await ElMessageBox.confirm('确定删除该广告屏吗？', '提示', { type: 'warning' })
    await screenDelete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(loadData)
</script>
