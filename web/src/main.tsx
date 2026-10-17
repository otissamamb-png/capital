import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import styles from './styles'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <style>{styles}</style>
    <App />
  </React.StrictMode>,
)
