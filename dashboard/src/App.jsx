import { BrowserRouter, Routes, Route } from 'react-router-dom'
import NavBar from './components/NavBar'
import Overview from './pages/Overview'
import JobTrends from './pages/JobTrends'
import Regressions from './pages/Regressions'
import FlakyTests from './pages/FlakyTests'
import Notifications from './pages/Notifications'

export default function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen flex flex-col">
        <NavBar />
        <main className="flex-1 max-w-7xl mx-auto w-full px-4 py-6">
          <Routes>
            <Route path="/" element={<Overview />} />
            <Route path="/trends" element={<JobTrends />} />
            <Route path="/regressions" element={<Regressions />} />
            <Route path="/flaky" element={<FlakyTests />} />
            <Route path="/notifications" element={<Notifications />} />
          </Routes>
        </main>
        <footer className="text-center text-xs text-gray-400 py-3">
          FlowGuard v1.0.0 — Self-hosted CI/CD Health Monitor
        </footer>
      </div>
    </BrowserRouter>
  )
}
