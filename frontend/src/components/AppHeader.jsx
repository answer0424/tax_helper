export function AppHeader({ loading }) {
  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">Hospital Tax Helper</p>
        <h1>병원 종소세 신고 보조</h1>
      </div>
      <div className="status-pill">{loading ? '처리 중' : '2단계 증빙 업로드'}</div>
    </header>
  )
}
