const API_BASE = 'http://localhost:8080/api'

async function request(path, options = {}) {
  const token = localStorage.getItem('forum_token')
  const headers = new Headers(options.headers || {})

  if (!(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  })

  const text = await response.text()
  const data = text ? JSON.parse(text) : null

  if (!response.ok) {
    throw new Error(data?.message || 'Request failed')
  }

  return data
}

export const api = {
  register: (payload) => request('/auth/register', { method: 'POST', body: JSON.stringify(payload) }),
  login: (payload) => request('/auth/login', { method: 'POST', body: JSON.stringify(payload) }),
  me: () => request('/auth/me'),
  topics: () => request('/forum/topics'),
  topic: (id) => request(`/forum/topics/${id}`),
  createTopic: (formData) => request('/forum/topics', { method: 'POST', body: formData }),
  addPost: (id, content) => request(`/forum/topics/${id}/posts?content=${encodeURIComponent(content)}`, { method: 'POST' }),
  deleteTopic: (id) => request(`/forum/topics/${id}`, { method: 'DELETE' }),
  news: () => request('/news'),
  newsItem: (id) => request(`/news/${id}`),
  createNews: (payload) => request('/news', { method: 'POST', body: JSON.stringify(payload) }),
  addComment: (id, payload) => request(`/news/${id}/comments`, { method: 'POST', body: JSON.stringify(payload) }),
  deleteNews: (id) => request(`/news/${id}`, { method: 'DELETE' }),
}
