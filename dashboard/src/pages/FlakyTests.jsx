import { useQuery } from '@tanstack/react-query'
import { fetchFlakyJobs } from '../api/flowguardApi'

export default function FlakyTests() {
  const { data = [], isLoading } = useQuery({
    queryKey: ['flakyJobs'],
    queryFn: () => fetchFlakyJobs(),
    refetchInterval: 30000,
  })

  function rateColor(rate) {
    if (rate > 0.5) return 'bg-red-100 text-red-700'
    if (rate > 0.3) return 'bg-orange-100 text-orange-700'
    return 'bg-yellow-100 text-yellow-700'
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Flaky Jobs</h1>
        <span className="bg-yellow-100 text-yellow-700 text-sm font-medium px-3 py-1 rounded-full">
          {data.length} flaky
        </span>
      </div>

      <div className="bg-white rounded-xl border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
            <tr>
              {['Job', 'Repo', 'Branch', 'Failure Rate', 'Failures', 'Total Runs', 'Last Updated'].map(h => (
                <th key={h} className="px-4 py-3 text-left">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y">
            {data.map(f => (
              <tr key={f.id} className="hover:bg-yellow-50">
                <td className="px-4 py-3 font-medium">{f.jobName}</td>
                <td className="px-4 py-3 font-mono text-xs text-gray-500">{f.repo}</td>
                <td className="px-4 py-3 text-xs">{f.headBranch}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded text-xs font-bold ${rateColor(f.failureRate)}`}>
                    {Math.round((f.failureRate ?? 0) * 100)}%
                  </span>
                </td>
                <td className="px-4 py-3">{f.failureCount}</td>
                <td className="px-4 py-3">{f.windowRuns}</td>
                <td className="px-4 py-3 text-xs text-gray-400">
                  {f.lastUpdated ? new Date(f.lastUpdated).toLocaleString() : '—'}
                </td>
              </tr>
            ))}
            {!isLoading && data.length === 0 && (
              <tr>
                <td colSpan={7} className="px-4 py-10 text-center text-gray-400">
                  No flaky jobs detected. Send more webhook events to build statistics.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
