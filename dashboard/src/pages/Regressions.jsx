import { useQuery } from '@tanstack/react-query'
import { fetchRegressions } from '../api/flowguardApi'

function formatDuration(secs) {
  if (secs == null) return '—'
  if (secs < 60) return `${Math.round(secs)}s`
  return `${Math.floor(secs / 60)}m ${Math.round(secs % 60)}s`
}

function formatDate(str) {
  if (!str) return '—'
  return new Date(str).toLocaleString()
}

export default function Regressions() {
  const { data = [], isLoading } = useQuery({
    queryKey: ['regressions'],
    queryFn: () => fetchRegressions(),
    refetchInterval: 30000,
  })

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Open Regressions</h1>
        <span className="bg-red-100 text-red-700 text-sm font-medium px-3 py-1 rounded-full">
          {data.length} open
        </span>
      </div>

      <div className="bg-white rounded-xl border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
            <tr>
              {['Job', 'Repo', 'Branch', 'Baseline', 'Observed', 'Increase', 'Detected'].map(h => (
                <th key={h} className="px-4 py-3 text-left">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y">
            {data.map(r => (
              <tr key={r.id} className="hover:bg-red-50">
                <td className="px-4 py-3 font-medium">{r.jobName}</td>
                <td className="px-4 py-3 font-mono text-xs text-gray-500">{r.repo}</td>
                <td className="px-4 py-3 text-xs">{r.headBranch}</td>
                <td className="px-4 py-3 text-green-700">{formatDuration(r.baselineSeconds)}</td>
                <td className="px-4 py-3 text-red-700 font-medium">{formatDuration(r.observedSeconds)}</td>
                <td className="px-4 py-3">
                  <span className="bg-red-100 text-red-700 px-2 py-0.5 rounded text-xs font-bold">
                    +{Math.round(r.increasePct ?? 0)}%
                  </span>
                </td>
                <td className="px-4 py-3 text-xs text-gray-400">{formatDate(r.detectedAt)}</td>
              </tr>
            ))}
            {!isLoading && data.length === 0 && (
              <tr>
                <td colSpan={7} className="px-4 py-10 text-center text-gray-400">
                  No open regressions. All jobs are performing normally.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
