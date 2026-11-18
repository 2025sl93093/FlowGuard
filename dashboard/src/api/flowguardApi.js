import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

export const fetchSummary = (repo) =>
  api.get('/analytics/summary', { params: { repo } }).then(r => r.data)

export const fetchRuns = (repo, page = 0) =>
  api.get('/pipelines/runs', { params: { repo, page, size: 20 } }).then(r => r.data)

export const fetchRepos = () =>
  api.get('/pipelines/repos').then(r => r.data)

export const fetchRegressions = (repo) =>
  api.get('/analytics/regressions', { params: { repo } }).then(r => r.data)

export const fetchFlakyJobs = (repo) =>
  api.get('/analytics/flaky', { params: { repo } }).then(r => r.data)

export const fetchJobHistory = (jobName, repo, branch = 'main') =>
  api.get(`/analytics/trends/${encodeURIComponent(jobName)}`, { params: { repo, branch } }).then(r => r.data)

export const fetchNotifications = (page = 0) =>
  api.get('/notifications', { params: { page, size: 20 } }).then(r => r.data)

export const sendTestEmail = () =>
  api.post('/notifications/test-email').then(r => r.data)
