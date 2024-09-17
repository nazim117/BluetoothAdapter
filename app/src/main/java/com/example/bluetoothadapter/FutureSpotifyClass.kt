package com.example.bluetoothadapter

class FutureSpotifyClass {
    //    private val authLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ){ result ->
//        val intent = result.data
//        val response = AuthorizationClient.getResponse(result.resultCode, intent)
//        handleSpotifyAuthResponse(response)
//    }

//    private fun handleSpotifyAuthResponse(response: AuthorizationResponse){
//        when(response.type){
//            AuthorizationResponse.Type.TOKEN -> {
//                val accesToken = response.accessToken
//                Log.d("SpotifyAuth", "Access token: $accesToken")
//            }
//            AuthorizationResponse.Type.ERROR -> {
//                Log.e("SpotifyAuth", "Authorization error: ${response.error}")
//            }
//            else -> {
//                Log.e("SpotifyAuth", "Authorization error: ${response.error}")
//            }
//
//        }
//    }

//    companion object {
//        const val REQUEST_CODE = 1337
//        private val CLIENT_ID = "658987877e844e18911f8f796969808e"
//        private val REDIRECT_URI = "bluetoothadapter://callback"
//    }
//
//    private fun authorizeSpotify(){
//        Log.d("SpotifyAuth", "authorizeSpotify() called")
//        val builder = AuthorizationRequest.Builder(CLIENT_ID,AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
//        builder.setScopes(arrayOf("streaming", "user-read-private", "user-read-email"))
//        val request = builder.build()
//
//        val intent = AuthorizationClient.createLoginActivityIntent(this, request)
//        authLauncher.launch(intent)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        Log.d("SpotifyAuth", "onActivity called with requestCode: $requestCode")
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == REQUEST_CODE) {
//            val response = AuthorizationClient.getResponse(resultCode, data)
//            when (response.type) {
//                AuthorizationResponse.Type.TOKEN -> {
//                    val accessToken = response.accessToken
//                    Log.d("SpotifyAuth", "Access Token: $accessToken")
//                    connectToSpotify(accessToken)
//                    // Use the access token for Spotify API or SDK operations
//                }
//                AuthorizationResponse.Type.ERROR -> {
//                    Log.e("SpotifyAuth", "Authorization error: ${response.error}")
//                }
//                else -> {
//                    Log.e("SpotifyAuth", "Unknown response type: ${response.type}")
//                }
//            }
//        }
//    }

//    private fun connectToSpotify(accesToken: String){
//        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
//            .setRedirectUri(REDIRECT_URI)
//            .showAuthView(true)
//            .build()
//
//        SpotifyAppRemote.connect(this, connectionParams, object: Connector.ConnectionListener {
//            override fun onConnected(spotifyAppRemote: SpotifyAppRemote?) {
//                Log.d("SpotifyAuth", "Connected to Spotify")
//            }
//
//            override fun onFailure(error: Throwable) {
//                Log.e("SpotifyAuth", "Failed to connect to Spotify: ${error.message}")
//            }
//        })
//    }
}