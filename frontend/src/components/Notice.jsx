export function Notice({ message, error }) {
  if (!message && !error) {
    return null
  }

  return <div className={`notice ${error ? 'error' : 'success'}`}>{error || message}</div>
}
