package ru.netology.nmedia.repository

//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.map
//import ru.netology.nmedia.dao.PostDao
//import ru.netology.nmedia.dto.Post
//import ru.netology.nmedia.entity.PostEntity
//
//class PostRepositoryRoomImpl(private val dao: PostDao) : PostRepository {
//
//    override fun get(): LiveData<List<Post>> = dao.getAll().map { list -> list.map { it.toDto() } }
//
//    override fun likeById(id: Long) = dao.likeById(id)
//
//    override fun toShareById(id: Long) = dao.toShareById(id)
//
//    override fun removeById(id: Long) = dao.removeById(id)
//
//    override fun save(post: Post) = dao.save(PostEntity.fromDto(post))
//}