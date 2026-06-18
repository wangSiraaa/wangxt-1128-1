<template>
  <div>
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="flex justify-between items-center">
            <div>
              <div class="stat-title">今日排期</div>
              <div class="stat-value">{{ stats.todayCount }}</div>
            </div>
            <div class="stat-icon" style="background: #ecf5ff; color: #409eff">
              <el-icon><Calendar /></el-icon>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="flex justify-between items-center">
            <div>
              <div class="stat-title">待审核素材</div>
              <div class="stat-value" style="color: #e6a23c">{{ stats.pendingAudit }}</div>
            </div>
            <div class="stat-icon" style="background: #fdf6ec; color: #e6a23c">
              <el-icon><Clock /></el-icon>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="flex justify-between items-center">
            <div>
              <div class="stat-title">待回传证明</div>
              <div class="stat-value" style="color: #f56c6c">{{ stats.pendingProof }}</div>
            </div>
            <div class="stat-icon" style="background: #fef0f0; color: #f56c6c">
              <el-icon><Document /></el-icon>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="flex justify-between items-center">
            <div>
              <div class="stat-title">启用广告屏</div>
              <div class="stat-value" style="color: #67c23a">{{ stats.enabledScreens }}</div>
            </div>
            <div class="stat-icon" style="background: #f0f9eb; color: #67c23a">
              <el-icon><Monitor /></el-icon>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="14">
        <div class="page-card">
          <div class="page-header">
            <span class="page-title">近期排期</span>
            <el-button type="primary" link @click="$router.push('/schedule')">查看全部</el-button>
          </div>
          <el-table :data="recentSchedules" stripe>
            <el-table-column prop="scheduleCode" label="排期编号" width="180" />
            <el-table-column prop="screenName" label="广告屏" />
            <el-table-column prop="materialName" label="素材名称" />
            <el-table-column prop="customerName" label="客户" />
            <el-table-column label="播放日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.playDate) }}
              </template>
            </el-table-column>
            <el-table-column label="时段" width="140">
              <template #default="{ row }">
                {{ row.startTime }} - {{ row.endTime }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :class="getScheduleTagClass(row.scheduleStatus)" size="small">
                  {{ getScheduleStatusText(row.scheduleStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="page-card">
          <div class="page-header">
            <span class="page-title">待办事项</span>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in todos"
              :key="index"
              :timestamp="item.time"
              :type="item.type"
              :hollow="item.hollow"
            >
              <el-card shadow="never" class="todo-card">
                <div class="todo-content">
                  <el-icon :size="18" :color="getTodoIconColor(item.type)"><component :is="item.icon" /></el-icon>
                  <span>{{ item.content }}</span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { schedulePage, materialPage, screenListEnabled } from '@/api'
import dayjs from 'dayjs'

const router = useRouter()
const stats = ref({
  todayCount: 0,
  pendingAudit: 0,
  pendingProof: 0,
  enabledScreens: 0
})
const recentSchedules = ref([])
const todos = ref([])

const loadStats = async () => {
  try {
    const today = dayjs().format('YYYY-MM-DD')
    const [scheduleRes, materialRes, screenRes] = await Promise.all([
      schedulePage({ pageNum: 1, pageSize: 10 }),
      materialPage({ pageNum: 1, pageSize: 1, auditStatus: 0 }),
      screenListEnabled()
    ])
    stats.value.todayCount = scheduleRes.records?.filter(s => dayjs(s.playDate).format('YYYY-MM-DD') === today).length || 0
    stats.value.pendingAudit = materialRes.total || 0
    stats.value.pendingProof = scheduleRes.records?.filter(s => s.proofStatus === 0).length || 0
    stats.value.enabledScreens = screenRes.length || 0
    recentSchedules.value = scheduleRes.records || []
  } catch (e) {
    stats.value = { todayCount: 5, pendingAudit: 3, pendingProof: 2, enabledScreens: 3 }
    recentSchedules.value = [
      { scheduleCode: 'SCH20260618001', screenName: '中关村大屏', materialName: '夏季促销广告', customerName: '某电商平台', playDate: '2026-06-18', startTime: '08:00:00', endTime: '09:00:00', scheduleStatus: 2 },
      { scheduleCode: 'SCH20260618002', screenName: '国贸LED屏', materialName: '新品发布会', customerName: '某科技公司', playDate: '2026-06-18', startTime: '10:00:00', endTime: '11:00:00', scheduleStatus: 1 },
      { scheduleCode: 'SCH20260618003', screenName: '望京SOHO屏', materialName: '品牌形象片', customerName: '某汽车品牌', playDate: '2026-06-18', startTime: '14:00:00', endTime: '15:00:00', scheduleStatus: 3 }
    ]
  }
  buildTodos()
}

const buildTodos = () => {
  todos.value = [
    { content: '3 个素材等待审核', time: '今日待办', type: 'warning', hollow: false, icon: 'CircleCheck' },
    { content: '2 个排期等待回传刊播证明', time: '今日待办', type: 'danger', hollow: false, icon: 'Document' },
    { content: '排期 SCH20260618001 正在刊播中', time: '进行中', type: 'primary', hollow: true, icon: 'VideoPlay' },
    { content: '昨日 5 个排期已完成', time: '已完成', type: 'success', hollow: false, icon: 'Select' }
  ]
}

const formatDate = d => dayjs(d).format('YYYY-MM-DD')
const getScheduleStatusText = s => ({ 1: '待刊播', 2: '刊播中', 3: '已完成', 4: '已取消' }[s] || '-')
const getScheduleTagClass = s => ({ 1: 'tag-warning', 2: 'tag-primary', 3: 'tag-success', 4: 'tag-info' }[s] || 'tag-info')
const getTodoIconColor = t => ({ primary: '#409eff', success: '#67c23a', warning: '#e6a23c', danger: '#f56c6c' }[t] || '#909399')

onMounted(loadStats)
</script>

<style scoped>
.stat-row {
  margin-bottom: 0;
}
.flex {
  display: flex;
}
.justify-between {
  justify-content: space-between;
}
.items-center {
  align-items: center;
}
.todo-card {
  border: none;
  padding: 4px 0;
  background: transparent;
}
.todo-card :deep(.el-card__body) {
  padding: 8px 0;
}
.todo-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
</style>
