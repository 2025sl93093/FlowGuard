import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { fetchRepos, fetchJobHistory, fetchJobBaseline } from '../api/flowguardApi'
import {
  LineChart, Line, XAxis, YAxis, Tooltip, ReferenceLine,
  CartesianGrid, ResponsiveContainer, Legend
} from 'recharts'

function formatDuration(secs) {
  if (!secs) return '0s'
  if (secs < 60) return `${secs}s`
  return `${Math.floor(secs / 60)}m ${secs % 60}s`
}

export default function JobTrends() {
  const [repo, setRepo] = useState('')
  const [jobName, setJobName] = useState('')
  const [jobNameInput, setJobNameInput] = useState('')
  const [branch, setBranch] = useState('main')

  useEffect(() => {
    const t = setTimeout(() => setJobName(jobNameInput), 500)
    return () => clearTimeout(t)
  }, [jobNameInput])

  const { data: repos = [] } = useQuery({ queryKey: ['repos'], queryFn: fetchRepos })

  const { data: history = [], isFetching } = useQuery({
    queryKey: ['jobHistory', repo, jobName, branch],
    queryFn: () => fetchJobHistory(jobName, repo, branch),
    enabled: !!jobName,
  })

  const { data: baselineData } = useQuery({
    queryKey: ['jobBaseline', repo, jobName, branch],
    queryFn: () => fetchJobBaseline(repo, jobName, branch),
    enabled: !!jobName && !!repo,
  })

  const chartData = history.map((j, i) => ({
    run: `#${j.runId ?? i + 1}`,
    duration: j.durationSeconds ?? 0,
    conclusion: j.conclusion,
  })).reverse()

  const avgBaseline = baselineData?.baselineSeconds
    ? Math.round(baselineData.baselineSeconds)
    : chartData.length > 0
      ? Math.round(chartData.slice(0, Math.min(10, chartData.length)).reduce((s, r) => s + r.duration, 0) / Math.min(10, chartData.length))
      : null

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-800">Job Trends</h1>

      <div className="bg-white rounded-xl border p-5 space-y-4">
        <div className="flex gap-4 flex-wrap">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Repository</label>
            <select
              className="border rounded px-3 py-1.5 text-sm"
              value={repo}
              onChange={e => setRepo(e.target.value)}
            >
              <option value="">All repos</option>
              {repos.map(r => <option key={r} value={r}>{r}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Job Name</label>
            <input
              className="border rounded px-3 py-1.5 text-sm w-48"
              placeholder="e.g. build-and-test"
              value={jobNameInput}
              onChange={e => setJobNameInput(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Branch</label>
            <input
              className="border rounded px-3 py-1.5 text-sm w-32"
              value={branch}
              onChange={e => setBranch(e.target.value)}
            />
          </div>
        </div>

        {isFetching && <p className="text-sm text-gray-400">Loading...</p>}

        {chartData.length > 0 ? (
          <>
            <h2 className="font-semibold text-gray-700">Duration History — last {chartData.length} runs</h2>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="run" tick={{ fontSize: 11 }} />
                <YAxis unit="s" tick={{ fontSize: 11 }} />
                <Tooltip formatter={(v) => formatDuration(v)} />
                <Legend />
                {avgBaseline && (
                  <ReferenceLine y={avgBaseline} stroke="#f59e0b" strokeDasharray="5 5"
                    label={{ value: `Baseline ~${formatDuration(avgBaseline)}${baselineData?.sampleCount ? ` (${baselineData.sampleCount} runs)` : ''}`, position: 'insideTopRight', fontSize: 11 }} />
                )}
                <Line type="monotone" dataKey="duration" stroke="#6366f1" strokeWidth={2}
                  dot={{ r: 3 }} activeDot={{ r: 5 }} name="Duration (s)" />
              </LineChart>
            </ResponsiveContainer>
          </>
        ) : jobName ? (
          <p className="text-gray-400 text-sm py-8 text-center">No data found for "{jobName}"</p>
        ) : (
          <p className="text-gray-400 text-sm py-8 text-center">Enter a job name to view its duration trend.</p>
        )}
      </div>
    </div>
  )
}
