package com.example.retrofitmvvm.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retrofitmvvm.model.PostModel
import com.example.retrofitmvvm.repo.PostRepository
import com.example.retrofitmvvm.repo.PostRepository.Companion.getPosts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostsViewModel(private val application: Application ) : ViewModel() {
    private val _mutableLiveData = MutableLiveData<List<PostModel>>()
    var posts = _mutableLiveData as LiveData<List<PostModel>>

    private val _stateFlow = MutableStateFlow<List<PostModel>>(listOf())
    var postsFlow = _stateFlow as StateFlow<List<PostModel>>

    private val _sendMutableLiveData = MutableLiveData<PostModel?>()
    var sendLiveData = _sendMutableLiveData

    suspend fun getPosts() {
        viewModelScope.launch {
            val response = async { PostRepository.getPosts() }
            if (response.await().isSuccessful) {
               _stateFlow.emit(response.await().body()!!)
            }
        }
    }


    fun sendPosts(map: Map<Any, Any>) {
        val sendCall = PostRepository.sendPost(map)
        sendCall.enqueue(object : Callback<PostModel> {
            override fun onResponse(call: Call<PostModel>, response: Response<PostModel>) {
                if (response.isSuccessful) {
                    _sendMutableLiveData.value = response.body()
                    Log.d("Headers", response.headers().value(0))
                } else {
                    _sendMutableLiveData.value = null
                }
            }

            override fun onFailure(call: Call<PostModel>, t: Throwable) {
                _sendMutableLiveData.value = null
            }
        })
    }
}