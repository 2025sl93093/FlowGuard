import { useQuery } from '@tanstack/react-query'
import { fetchSummary, fetchRuns } from '../api/flowguardApi'
import SummaryCard from '../components/SummaryCard'
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, Cell } from 'recharts'

function formatAgo(dateStr) {
  if (!dateStr) return '—'
  const diff = Math.floor((Date.now() - new Date(dateStr + 'Z')) / 1000)
  if (diff < 60) return `${diff}s ago`
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
  return `${Math.floor(diff / 86400)}d ago`
}

function formatDuration(secs) {
  if (!secs) return '—'
  if (secs < 60) return `${secs}s`
  return `${Math.floor(secs / 60)}m ${secs % 60}s`
}

const conclusionColor = { success: '#22c55e', failure: '#ef4444', cancelled: '#94a3b8' }

export default function Overview() {
  const { data: summary } = useQuery({
    queryKey: ['summary'],
    queryFn: () => fetchSummary(),
    refetchInterval: 30000,
  })
  const { data: runsPage } = useQuery({
    queryKey: ['runs'],
    queryFn: () => fetchRuns(),
    refetchInterval: 30000,
  })

  const runs = runsPage?.content ?? []

  // Build daily bar chart data from last 10 runs
  const chartData = runs.slice(0, 10).reverse().map((r, i) => ({
    name: `#${r.runNumber ?? i + 1}`,
    duration: r.durationSeconds ?? 0,
    conclusion: r.conclusion,
  }))

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-800">Overview</h1>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <SummaryCard title="Total Runs" value={runsPage?.totalElements ?? 0} color="indigo" />
        <SummaryCard
          title="Success Rate"
          value={runs.length ? `${Math.round((runs.filter(r => r.conclusion === 'success').length / runs.length) * 100)}%` : '—'}
          color="green"
        />
        <SummaryCard title="Open Regressions" value={summary?.openRegressions ?? 0} color="red" />
        <SummaryCard title="Flaky Jobs" value={summary?.flakyJobs ?? 0} color="yellow" />
      </div>

      {chartData.length > 0 && (
        <div className="bg-white rounded-xl border p-5">
          <h2 className="font-semibold text-gray-700 mb-4">Recent Run Durations</h2>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={chartData}>
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis unit="s" tick={{ fontSize: 12 }} />
              <Tooltip formatter={(v) => formatDuration(v)} />
              <Bar dataKey="duration" radius={[4, 4, 0, 0]}>
                {chartData.map((entry, i) => (
                  <Cell key={i} fill={conclusionColor[entry.conclusion] ?? '#94a3b8'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      <div className="bg-white rounded-xl border overflow-hidden">
        <div className="px-5 py-3 border-b bg-gray-50 font-semibold text-sm text-gray-600">Recent Runs</div>
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
            <tr>
              {['Repo', 'Workflow', 'Branch', 'Duration', 'Conclusion', 'When'].map(h => (
                <th key={h} className="px-4 py-2 text-left">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y">
            {runs.slice(0, 10).map(run => (
              <tr key={run.id} className="hover:bg-gray-50">
                <td className="px-4 py-2 font-mono text-xs">
                  <a href={`https://github.com/${run.repo}`} target="_blank" rel="noreferrer" className="hover:text-blue-600 hover:underline">
                    {run.repo}
                  </a>
                </td>
                <td className="px-4 py-2">{run.workflowName}</td>
                <td className="px-4 py-2 text-xs">{run.headBranch}</td>
                <td className="px-4 py-2">{formatDuration(run.durationSeconds)}</td>
                <td className="px-4 py-2">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                    run.conclusion === 'success' ? 'bg-green-100 text-green-700' :
                    run.conclusion === 'failure' ? 'bg-red-100 text-red-700' :
                    'bg-gray-100 text-gray-600'
                  }`}>
                    {run.conclusion ?? 'unknown'}
                  </span>
                </td>
                <td className="px-4 py-2 text-gray-400 text-xs">{formatAgo(run.updatedAt)}</td>
              </tr>
            ))}
            {runs.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  No runs yet. Send a webhook event to get started.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
