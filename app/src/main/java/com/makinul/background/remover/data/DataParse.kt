package com.makinul.background.remover.data

import com.makinul.background.remover.utils.AppConstants
import com.mmh.emmahealth.data.Resource
import com.makinul.background.remover.data.network.RetrofitResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.makinul.background.remover.utils.NetworkUtil
import javax.inject.Inject

class DataParse @Inject constructor(
    private val networkUtil: NetworkUtil
) {
    fun <T> responseParse(response: RetrofitResponse<T>): Resource<T> {
        return if (response.statusCode == AppConstants.STATUS_OK) {
            Resource.success(response.data!!)
        } else {
            Resource.error(response.statusMessage)
        }
    }

//    fun <T> responseParseTest(response: RetrofitResponse<T>): Flow<Data<T>> {
//        return flow<Data<T>> {
//            if (response.statusCode == AppConstants.STATUS_OK && response.data != null) {
//                Data.Success(response.data!!)
//            } else {
//                Data.Error(response.statusMessage)
//            }
//        }.flowOn(Dispatchers.IO)
//    }

    fun <T> exceptionParse(msg: String?): Resource<T> {
        msg?.let {
            if (msg.contains(AppConstants.UNABLE_TO_RESOLVE_HOST, true)
                && !networkUtil.isInternetAvailable()
            ) {
                return Resource.error(AppConstants.NO_INTERNET)
            }
        }
        return Resource.error("Unexpected error, please try again")
    }

    fun <T> exceptionParseData(msg: String?): Data<T> {
        msg?.let {
            if (msg.contains(AppConstants.UNABLE_TO_RESOLVE_HOST, true)
                && !networkUtil.isInternetAvailable()
            ) {
                return Data.Error(AppConstants.NO_INTERNET)
            }
        }
        return Data.Error("Unexpected error, please try again")
    }

    fun exceptionParseTest(msg: String?): Flow<Data<Nothing>> {
        return flow {
            msg?.let {
                if (msg.contains(AppConstants.UNABLE_TO_RESOLVE_HOST, true)
                    && !networkUtil.isInternetAvailable()
                ) {
                    emit(Data.Error(AppConstants.NO_INTERNET))
                }
            }
            emit(Data.Error("Unexpected error, please try again"))
        }.flowOn(Dispatchers.IO)
    }
}