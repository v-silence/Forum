import { useEffect, useMemo, useState } from 'react'
import { api } from './api'

const emptyTopicForm = { title: '', content: '', files: [] }
const emptyNewsForm = { title: '', content: '' }
const rolePriority = ['ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_USER']

function useSession() {
  const [session, setSession] = useState(() => {
    const token = localStorage.getItem('forum_token')
    const user = localStorage.getItem('forum_user')
    return { token, user: user ? JSON.parse(user) : null }
  })

  const save = (auth) => {
    if (auth?.token) {
      localStorage.setItem('forum_token', auth.token)
    }
    if (auth?.user) {
      localStorage.setItem('forum_user', JSON.stringify(auth.user))
    }
    setSession({ token: auth?.token || session.token, user: auth?.user || session.user })
  }

  const logout = () => {
    localStorage.removeItem('forum_token')
    localStorage.removeItem('forum_user')
    setSession({ token: null, user: null })
  }

  return { ...session, save, logout }
}

function App() {
  const session = useSession()
  const [topics, setTopics] = useState([])
  const [news, setNews] = useState([])
  const [topicQuery, setTopicQuery] = useState('')
  const [newsQuery, setNewsQuery] = useState('')
  const [selectedTopicId, setSelectedTopicId] = useState(null)
  const [selectedNewsId, setSelectedNewsId] = useState(null)
  const [topicDetail, setTopicDetail] = useState(null)
  const [newsDetail, setNewsDetail] = useState(null)
  const [authMode, setAuthMode] = useState('login')
  const [authForm, setAuthForm] = useState({ username: '', email: '', password: '' })
  const [topicForm, setTopicForm] = useState(emptyTopicForm)
  const [newsForm, setNewsForm] = useState(emptyNewsForm)
  const [postText, setPostText] = useState('')
  const [commentDrafts, setCommentDrafts] = useState({ root: '' })
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  const roles = session.user?.roles || []
  const canCreateNews = roles.includes('ROLE_ADMIN') || roles.includes('ROLE_MODERATOR')
  const canModerate = canCreateNews
  const currentRole = rolePriority.find((role) => roles.includes(role))?.replace('ROLE_', '') || 'guest'

  const forumStats = useMemo(() => ({
    topics: topics.length,
    replies: topics.reduce((sum, topic) => sum + (topic.posts?.length || 0), 0),
    news: news.length,
  }), [topics, news])

  const filteredTopics = useMemo(() => {
    const query = topicQuery.trim().toLowerCase()
    if (!query) {
      return topics
    }
    return topics.filter((topic) =>
      `${topic.title} ${topic.author.username}`.toLowerCase().includes(query),
    )
  }, [topics, topicQuery])

  const filteredNews = useMemo(() => {
    const query = newsQuery.trim().toLowerCase()
    if (!query) {
      return news
    }
    return news.filter((item) =>
      `${item.title} ${item.author.username}`.toLowerCase().includes(query),
    )
  }, [news, newsQuery])

  async function loadOverview() {
    const [topicsData, newsData] = await Promise.all([api.topics(), api.news()])
    setTopics(topicsData)
    setNews(newsData)
    if (!selectedTopicId && topicsData[0]) {
      setSelectedTopicId(topicsData[0].id)
    }
    if (!selectedNewsId && newsData[0]) {
      setSelectedNewsId(newsData[0].id)
    }
  }

  useEffect(() => {
    loadOverview().catch((error) => setMessage(error.message))
  }, [])

  useEffect(() => {
    if (!selectedTopicId) {
      setTopicDetail(null)
      return
    }
    api.topic(selectedTopicId)
      .then(setTopicDetail)
      .catch((error) => setMessage(error.message))
  }, [selectedTopicId])

  useEffect(() => {
    if (!selectedNewsId) {
      setNewsDetail(null)
      return
    }
    api.newsItem(selectedNewsId)
      .then(setNewsDetail)
      .catch((error) => setMessage(error.message))
  }, [selectedNewsId])

  useEffect(() => {
    if (!session.token) {
      return
    }
    api.me()
      .then((auth) => session.save(auth))
      .catch(() => session.logout())
  }, [])

  async function handleAuthSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setMessage('')
    try {
      const action = authMode === 'login' ? api.login : api.register
      const payload = authMode === 'login'
        ? { username: authForm.username, password: authForm.password }
        : authForm
      const auth = await action(payload)
      session.save(auth)
      setAuthForm({ username: '', email: '', password: '' })
      setMessage(authMode === 'login' ? 'Signed in successfully' : 'Registration completed')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleTopicSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setMessage('')
    try {
      const formData = new FormData()
      formData.append('title', topicForm.title)
      formData.append('content', topicForm.content)
      Array.from(topicForm.files || []).forEach((file) => formData.append('files', file))
      const created = await api.createTopic(formData)
      setTopicForm(emptyTopicForm)
      setSelectedTopicId(created.id)
      await loadOverview()
      setMessage('Topic created')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  async function handlePostSubmit(event) {
    event.preventDefault()
    if (!selectedTopicId) return
    setLoading(true)
    setMessage('')
    try {
      await api.addPost(selectedTopicId, postText)
      setPostText('')
      setTopicDetail(await api.topic(selectedTopicId))
      await loadOverview()
      setMessage('Reply added')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleNewsSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setMessage('')
    try {
      const created = await api.createNews(newsForm)
      setNewsForm(emptyNewsForm)
      setSelectedNewsId(created.id)
      await loadOverview()
      setMessage('News published')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleCommentSubmit(parentCommentId = null) {
    const key = parentCommentId ?? 'root'
    const content = commentDrafts[key]
    if (!selectedNewsId || !content?.trim()) return
    setLoading(true)
    setMessage('')
    try {
      await api.addComment(selectedNewsId, { parentCommentId, content })
      setCommentDrafts((prev) => ({ ...prev, [key]: '' }))
      setNewsDetail(await api.newsItem(selectedNewsId))
      await loadOverview()
      setMessage(parentCommentId ? 'Reply added' : 'Comment added')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleDeleteTopic(topicId) {
    if (!window.confirm('Delete this topic? This action cannot be undone.')) {
      return
    }
    setLoading(true)
    setMessage('')
    try {
      await api.deleteTopic(topicId)
      const nextTopics = topics.filter((topic) => topic.id !== topicId)
      setTopics(nextTopics)
      if (selectedTopicId === topicId) {
        const nextId = nextTopics[0]?.id ?? null
        setSelectedTopicId(nextId)
        setTopicDetail(nextId ? await api.topic(nextId) : null)
      }
      await loadOverview()
      setMessage('Topic deleted')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleDeleteNews(newsId) {
    if (!window.confirm('Delete this news post? This action cannot be undone.')) {
      return
    }
    setLoading(true)
    setMessage('')
    try {
      await api.deleteNews(newsId)
      const nextNews = news.filter((item) => item.id !== newsId)
      setNews(nextNews)
      if (selectedNewsId === newsId) {
        const nextId = nextNews[0]?.id ?? null
        setSelectedNewsId(nextId)
        setNewsDetail(nextId ? await api.newsItem(nextId) : null)
      }
      await loadOverview()
      setMessage('News deleted')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-shell">
      <header className="hero">
        <div>
          <p className="eyebrow">Студенческий форум</p>
          <h1>Пространство для обсуждений</h1>
          <p className="hero-copy">
            Минимальная версия с ролями, лентой, загрузкой файлов и комментариями
          </p>
        </div>
        <div className="hero-stats">
          <StatCard value={forumStats.topics} label="Треды" />
          <StatCard value={forumStats.news} label="Новости" />
          <StatCard value={currentRole} label="Текущая роль" />
        </div>
      </header>

      <main className="dashboard">
        <section className="panel auth-panel">
          <div className="panel-header">
            <h2>Access</h2>
            {session.user && <button className="ghost-button" onClick={session.logout}>Sign out</button>}
          </div>
          {session.user ? (
            <div className="user-card">
              <strong>{session.user.username}</strong>
              <span>{session.user.email}</span>
              <small>{roles.join(', ')}</small>
            </div>
          ) : (
            <>
              <div className="switcher">
                <button className={authMode === 'login' ? 'active' : ''} onClick={() => setAuthMode('login')}>Login</button>
                <button className={authMode === 'register' ? 'active' : ''} onClick={() => setAuthMode('register')}>Register</button>
              </div>
              <form className="stack-form" onSubmit={handleAuthSubmit}>
                <input placeholder="Имя пользователя" value={authForm.username} onChange={(e) => setAuthForm({ ...authForm, username: e.target.value })} />
                {authMode === 'register' && (
                  <input placeholder="Электронная почта" type="email" value={authForm.email} onChange={(e) => setAuthForm({ ...authForm, email: e.target.value })} />
                )}
                <input placeholder="Пароль" type="password" value={authForm.password} onChange={(e) => setAuthForm({ ...authForm, password: e.target.value })} />
                <button type="submit" disabled={loading}>{authMode === 'login' ? 'Sign in' : 'Create account'}</button>
              </form>
            </>
          )}
          <div className="seed-box">
            <span>Данные для входа</span>
            <code>admin/admin123</code>
            <code>moderator/mod12345</code>
            <code>student/student123</code>
          </div>
          {message && <p className="message-bar">{message}</p>}
        </section>

        <div className="content-grid">
          <section className="panel forum-panel">
            <div className="panel-header">
              <h2>Форум</h2>
              <span>{topics.length} Треды</span>
            </div>
            <div className="panel-tools">
              <input
                placeholder="Найти тред"
                value={topicQuery}
                onChange={(e) => setTopicQuery(e.target.value)}
              />
            </div>
            <div className="forum-layout panel-scroll-frame">
              <div className="list-column scroll-column">
                {filteredTopics.map((topic) => (
                  <button key={topic.id} className={`list-item ${selectedTopicId === topic.id ? 'selected' : ''}`} onClick={() => setSelectedTopicId(topic.id)}>
                    <div className="item-header">
                      <strong>{topic.title}</strong>
                      {canModerate && (
                        <button
                          type="button"
                          className="danger-button"
                          onClick={(event) => {
                            event.stopPropagation()
                            handleDeleteTopic(topic.id)
                          }}
                        >
                          Удалить
                        </button>
                      )}
                    </div>
                    <span>{topic.author.username}</span>
                    <small>{new Date(topic.createdAt).toLocaleString()}</small>
                  </button>
                ))}
                {filteredTopics.length === 0 && <p className="empty-state">Не найдено.</p>}
              </div>
              <div className="detail-column scroll-column">
                {topicDetail ? (
                  <>
                    <div className="detail-card accent-card">
                      <div className="detail-header">
                        <h3>{topicDetail.title}</h3>
                        {canModerate && (
                          <button type="button" className="danger-button" onClick={() => handleDeleteTopic(topicDetail.id)}>
                            Удалить
                          </button>
                        )}
                      </div>
                      <p>{topicDetail.content}</p>
                      <small>Author: {topicDetail.author.username}</small>
                      {topicDetail.attachments.length > 0 && (
                        <div className="file-row">
                          {topicDetail.attachments.map((file) => (
                            <a key={file.id} href={`http://localhost:8080${file.downloadUrl}`} target="_blank" rel="noreferrer">{file.originalFilename}</a>
                          ))}
                        </div>
                      )}
                    </div>
                    <div className="thread-list">
                      {topicDetail.posts.map((post) => (
                        <article key={post.id} className="thread-card">
                          <div>
                            <strong>{post.author.username}</strong>
                            <small>{new Date(post.createdAt).toLocaleString()}</small>
                          </div>
                          <p>{post.content}</p>
                        </article>
                      ))}
                    </div>
                    {session.user && (
                      <form className="stack-form section-form" onSubmit={handlePostSubmit}>
                        <textarea rows="4" placeholder="Написать ответ" value={postText} onChange={(e) => setPostText(e.target.value)} />
                        <button type="submit" disabled={loading || !postText.trim()}>Опубликовать ответ</button>
                      </form>
                    )}
                  </>
                ) : (
                  <p className="empty-state">No topic selected yet.</p>
                )}
              </div>
            </div>
            {session.user && (
              <form className="stack-form top-border" onSubmit={handleTopicSubmit}>
                <h3>Создать тред</h3>
                <input placeholder="Название" value={topicForm.title} onChange={(e) => setTopicForm({ ...topicForm, title: e.target.value })} />
                <textarea rows="4" placeholder="Описание" value={topicForm.content} onChange={(e) => setTopicForm({ ...topicForm, content: e.target.value })} />
                <input type="file" multiple onChange={(e) => setTopicForm({ ...topicForm, files: e.target.files })} />
                <button type="submit" disabled={loading}>Создать тред</button>
              </form>
            )}
          </section>

          <section className="panel news-panel">
            <div className="panel-header">
              <h2>Новости</h2>
              <span>{news.length} posts</span>
            </div>
            <div className="panel-tools">
              <input
                placeholder="Искать новость"
                value={newsQuery}
                onChange={(e) => setNewsQuery(e.target.value)}
              />
            </div>
            <div className="forum-layout panel-scroll-frame">
              <div className="list-column scroll-column">
                {filteredNews.map((item) => (
                  <button key={item.id} className={`list-item ${selectedNewsId === item.id ? 'selected' : ''}`} onClick={() => setSelectedNewsId(item.id)}>
                    <div className="item-header">
                      <strong>{item.title}</strong>
                      {canModerate && (
                        <button
                          type="button"
                          className="danger-button"
                          onClick={(event) => {
                            event.stopPropagation()
                            handleDeleteNews(item.id)
                          }}
                        >
                          Удалить
                        </button>
                      )}
                    </div>
                    <span>{item.author.username}</span>
                    <small>{new Date(item.createdAt).toLocaleString()}</small>
                  </button>
                ))}
                {filteredNews.length === 0 && <p className="empty-state">No news matches the current filter.</p>}
              </div>
              <div className="detail-column scroll-column">
                {newsDetail ? (
                  <>
                    <div className="detail-card news-card">
                      <div className="detail-header">
                        <h3>{newsDetail.title}</h3>
                        {canModerate && (
                          <button type="button" className="danger-button" onClick={() => handleDeleteNews(newsDetail.id)}>
                            Delete news
                          </button>
                        )}
                      </div>
                      <p>{newsDetail.content}</p>
                      <small>Published by: {newsDetail.author.username}</small>
                    </div>
                    <div className="comment-box">
                      {newsDetail.comments.map((comment) => (
                        <CommentThread
                          key={comment.id}
                          comment={comment}
                          drafts={commentDrafts}
                          setDrafts={setCommentDrafts}
                          canReply={Boolean(session.user)}
                          onSubmit={handleCommentSubmit}
                        />
                      ))}
                    </div>
                    {session.user && (
                      <form className="stack-form section-form" onSubmit={(e) => { e.preventDefault(); handleCommentSubmit(null) }}>
                        <textarea rows="3" placeholder="Добавить комментарий" value={commentDrafts.root || ''} onChange={(e) => setCommentDrafts((prev) => ({ ...prev, root: e.target.value }))} />
                        <button type="submit" disabled={loading || !(commentDrafts.root || '').trim()}>Добавить комментарий</button>
                      </form>
                    )}
                  </>
                ) : (
                  <p className="empty-state">Ни одна новость не выбрана</p>
                )}
              </div>
            </div>
            {session.user && canCreateNews && (
              <form className="stack-form top-border" onSubmit={handleNewsSubmit}>
                <h3>Publish news</h3>
                <input placeholder="Заголовок новости" value={newsForm.title} onChange={(e) => setNewsForm({ ...newsForm, title: e.target.value })} />
                <textarea rows="4" placeholder="Основной текст" value={newsForm.content} onChange={(e) => setNewsForm({ ...newsForm, content: e.target.value })} />
                <button type="submit" disabled={loading}>Опубликовать</button>
              </form>
            )}
          </section>
        </div>
      </main>
    </div>
  )
}

function StatCard({ value, label }) {
  return (
    <div className="stat-card">
      <strong>{value}</strong>
      <span>{label}</span>
    </div>
  )
}

function CommentThread({ comment, drafts, setDrafts, canReply, onSubmit }) {
  const draft = drafts[comment.id] || ''
  return (
    <div className="comment-thread">
      <article className="thread-card">
        <div>
          <strong>{comment.author.username}</strong>
          <small>{new Date(comment.createdAt).toLocaleString()}</small>
        </div>
        <p>{comment.content}</p>
      </article>
      {canReply && (
        <form className="reply-form" onSubmit={(e) => { e.preventDefault(); onSubmit(comment.id) }}>
          <input placeholder="Написать ответ" value={draft} onChange={(e) => setDrafts((prev) => ({ ...prev, [comment.id]: e.target.value }))} />
          <button type="submit" disabled={!draft.trim()}>Reply</button>
        </form>
      )}
      {comment.replies?.length > 0 && (
        <div className="reply-stack">
          {comment.replies.map((reply) => (
            <CommentThread key={reply.id} comment={reply} drafts={drafts} setDrafts={setDrafts} canReply={canReply} onSubmit={onSubmit} />
          ))}
        </div>
      )}
    </div>
  )
}

export default App

