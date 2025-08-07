package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.error.ErrorCode400And500
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File

class SignUpViewModel(application: Application): AndroidViewModel(application) {

    private val noPhoto = PhotoModel()
    private val dao: PostDao = AppDb.getInstance(application).postDao
    private val repository: PostRepository = PostRepositoryImpl(dao)
    private val _authState = MutableLiveData(AuthState())
    val authState: LiveData<AuthState>
        get() =_authState
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo
    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    private val _bottomSheet = SingleLiveEvent<Unit>()
    val bottomSheet: LiveData<Unit>
        get() = _bottomSheet

    fun signUp(userName: String, login: String, password: String) {
        viewModelScope.launch{
            try {
                when (_photo.value) {
                    noPhoto -> _authState.value = repository.signUp(userName, login, password)
                    else -> _photo.value?.file?.let { file ->
                        _authState.value = repository.signUpWithAPhoto(userName, login, password, MediaUpload(file))
                    }
                }
            } catch (e: ErrorCode400And500) {
                _bottomSheet.value = Unit
            } catch (e: UnknownError) {
                _dataState.value = FeedModelState(errorCode300 = true)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }
}