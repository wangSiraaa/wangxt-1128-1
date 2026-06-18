import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue')
  },
  {
    path: '/material',
    name: 'Material',
    component: () => import('@/views/Material.vue')
  },
  {
    path: '/audit',
    name: 'Audit',
    component: () => import('@/views/Audit.vue')
  },
  {
    path: '/schedule',
    name: 'Schedule',
    component: () => import('@/views/Schedule.vue')
  },
  {
    path: '/proof',
    name: 'Proof',
    component: () => import('@/views/Proof.vue')
  },
  {
    path: '/screen',
    name: 'Screen',
    component: () => import('@/views/Screen.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
