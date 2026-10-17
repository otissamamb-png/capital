import { useEffect, useState } from 'react'
import { supabase, Post, Comment } from '../lib/supabase'
import { useAuth, timeAgo } from '../auth'

export default function CommunityScreen() {
  const { session, profile } = useAuth()
  const [posts, setPosts] = useState<Post[]>([])
  const [likedIds, setLikedIds] = useState<Set<string>>(new Set())
  const [newPost, setNewPost] = useState('')
  const [postType, setPostType] = useState('General')
  const [loading, setLoading] = useState(true)
  const [openComments, setOpenComments] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    ;(async () => {
      const [postsRes, likesRes] = await Promise.all([
        supabase.from('posts').select('*').order('created_at', { ascending: false }),
        session
          ? supabase.from('post_likes').select('post_id').eq('user_id', session.user.id)
          : Promise.resolve({ data: [] as any[] }),
      ])
      setPosts(postsRes.data ?? [])
      setLikedIds(new Set((likesRes.data ?? []).map((l: any) => l.post_id)))
      setLoading(false)
    })()
  }, [session])

  async function createPost(e: React.FormEvent) {
    e.preventDefault()
    if (!newPost.trim() || !session) return
    setBusy(true)
    const { error } = await supabase.from('posts').insert({
      author_name: profile?.full_name || 'Resident',
      content: newPost.trim(),
      post_type: postType,
    })
    setBusy(false)
    if (!error) {
      setNewPost('')
      const { data } = await supabase.from('posts').select('*').order('created_at', { ascending: false })
      setPosts(data ?? [])
    }
  }

  async function toggleLike(post: Post) {
    if (!session) return
    const liked = likedIds.has(post.id)
    if (liked) {
      await supabase.from('post_likes').delete().eq('post_id', post.id).eq('user_id', session.user.id)
      setLikedIds(prev => { const n = new Set(prev); n.delete(post.id); return n })
    } else {
      await supabase.from('post_likes').insert({ post_id: post.id })
      setLikedIds(prev => new Set(prev).add(post.id))
    }
    await supabase.from('posts').update({ likes_count: post.likes_count + (liked ? -1 : 1) }).eq('id', post.id)
    setPosts(prev => prev.map(p => p.id === post.id ? { ...p, likes_count: p.likes_count + (liked ? -1 : 1) } : p))
  }

  async function deletePost(id: string) {
    await supabase.from('posts').delete().eq('id', id)
    setPosts(prev => prev.filter(p => p.id !== id))
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>

  return (
    <div>
      <h1 className="section-title">Community</h1>
      <p className="section-sub">Connect with fellow residents</p>

      {session && (
        <form onSubmit={createPost} className="card" style={{ marginBottom: 20 }}>
          <textarea
            className="field"
            rows={3}
            placeholder="Share something with the residence…"
            value={newPost}
            onChange={e => setNewPost(e.target.value)}
          />
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 10 }}>
            <select className="field" style={{ width: 'auto' }} value={postType} onChange={e => setPostType(e.target.value)}>
              {['General', 'Announcement', 'Question', 'Event'].map(t => <option key={t}>{t}</option>)}
            </select>
            <button className="btn btn-primary" disabled={busy || !newPost.trim()}>
              {busy ? 'Posting…' : 'Post'}
            </button>
          </div>
        </form>
      )}

      {posts.length === 0 ? (
        <div className="empty-state"><div className="empty-icon">💬</div>No posts yet. Start the conversation!</div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {posts.map(p => (
            <div key={p.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <div className="avatar">{(p.author_name || 'U').charAt(0).toUpperCase()}</div>
                  <div>
                    <strong>{p.author_name}</strong>
                    <p style={{ fontSize: 12, color: 'var(--slate-400)' }}>{timeAgo(p.created_at)} • {p.post_type}</p>
                  </div>
                </div>
                {session?.user.id === p.author_id && (
                  <button className="btn btn-danger btn-sm" onClick={() => deletePost(p.id)}>Delete</button>
                )}
              </div>
              <p style={{ marginTop: 10, fontSize: 14 }}>{p.content}</p>
              <div style={{ display: 'flex', gap: 12, marginTop: 12, alignItems: 'center' }}>
                <button
                  className={`btn btn-sm ${likedIds.has(p.id) ? 'btn-primary' : 'btn-outline'}`}
                  onClick={() => toggleLike(p)}
                >♥ {p.likes_count}</button>
                <button className="btn btn-outline btn-sm" onClick={() => setOpenComments(openComments === p.id ? null : p.id)}>
                  💬 {p.comments_count} Comments
                </button>
              </div>
              {openComments === p.id && <CommentsSection postId={p.id} />}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function CommentsSection({ postId }: { postId: string }) {
  const { session, profile } = useAuth()
  const [comments, setComments] = useState<Comment[]>([])
  const [text, setText] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    ;(async () => {
      const { data } = await supabase.from('comments').select('*').eq('post_id', postId).order('created_at')
      setComments(data ?? [])
      setLoading(false)
    })()
  }, [postId])

  async function addComment(e: React.FormEvent) {
    e.preventDefault()
    if (!text.trim() || !session) return
    const { error } = await supabase.from('comments').insert({
      post_id: postId,
      user_name: profile?.full_name || '',
      content: text.trim(),
    })
    if (error) return
    setText('')
    const { data } = await supabase.from('comments').select('*').eq('post_id', postId).order('created_at')
    setComments(data ?? [])
    await supabase.from('posts').update({ comments_count: comments.length + 1 }).eq('id', postId)
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>

  return (
    <div style={{ marginTop: 12, paddingTop: 12, borderTop: '1px solid var(--slate-200)' }}>
      {comments.length === 0 ? (
        <p style={{ fontSize: 13, color: 'var(--slate-400)', marginBottom: 10 }}>No comments yet.</p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 10 }}>
          {comments.map(c => (
            <div key={c.id} style={{ background: 'var(--slate-50)', padding: '8px 12px', borderRadius: 10 }}>
              <strong style={{ fontSize: 13 }}>{c.user_name}</strong>
              <p style={{ fontSize: 13 }}>{c.content}</p>
            </div>
          ))}
        </div>
      )}
      {session && (
        <form onSubmit={addComment} style={{ display: 'flex', gap: 8 }}>
          <input className="field" placeholder="Add a comment…" value={text} onChange={e => setText(e.target.value)} />
          <button className="btn btn-primary btn-sm">Send</button>
        </form>
      )}
    </div>
  )
}
