import axios from 'axios'

// Same-origin in production (served by Spring); via Vite proxy in dev.
// Spring issues an XSRF-TOKEN cookie; axios echoes it back automatically on mutating requests.
const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN'
})

export default api
