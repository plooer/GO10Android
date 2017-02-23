package th.co.gosoft.go10.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import th.co.gosoft.go10.R;
import th.co.gosoft.go10.adapter.TopicAdapter;
import th.co.gosoft.go10.model.LikeModel;
import th.co.gosoft.go10.util.OnDataPass;
import th.co.gosoft.go10.util.PropertyUtility;

public class BoardContentFragment extends Fragment implements OnDataPass {

    private final String LOG_TAG = "BoardContentFragmentTag";

    private String GET_TOPIC_URL;
    private String CHECK_LIKE_URL;
    private String LIKE_URL;
    private ProgressDialog progress;
    private String _id ;
    private String empEmail;
    private String room_id ;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private boolean isLoadTopicDone = false;
    private boolean isCheckLikeDone = false;
    private List<Map> topicModelMap;
    private LikeModel likeModel;
    private boolean canComent;
    private ListView commentListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate : BoardContentFragment");
        super.onCreate(savedInstanceState);

        GET_TOPIC_URL = PropertyUtility.getProperty("httpUrlSite", getActivity())+"GO10WebService/api/"+PropertyUtility.getProperty("versionServer", getActivity())
                +"topic/gettopicbyid";
        CHECK_LIKE_URL = PropertyUtility.getProperty("httpUrlSite", getActivity())+"GO10WebService/api/"+PropertyUtility.getProperty("versionServer", getActivity())
                +"topic/checkLikeTopic";
        LIKE_URL = PropertyUtility.getProperty("httpUrlSite", getActivity())+"GO10WebService/api/"+PropertyUtility.getProperty("versionServer", getActivity())
                +"topic/";

        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.activity_board_content, container, false);
        Bundle bundle = getArguments();
        _id = bundle.getString("_id");
        empEmail = sharedPref.getString("empEmail", null);
        Log.i(LOG_TAG, "_id : " + _id);

        return view;
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
            commentListView = (ListView) getView().findViewById(R.id.commentListView);
            Log.i(LOG_TAG, "onStart");
            callGetWebService();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onStop() {
        try {
            super.onStop();
            Log.i(LOG_TAG, "onStop");
            LikeModel likeModel = createLikeModelFromSharedPreferences();
            String webServiceURL = LIKE_URL;
            if(this.likeModel == null && likeModel.isStatusLike() == true) {
                Log.i(LOG_TAG, "New Like");
                webServiceURL = webServiceURL+"newLike";
                callPostWebService(likeModel, webServiceURL);
            } else if(this.likeModel != null && this.likeModel.isStatusLike() != likeModel.isStatusLike()) {
                if(likeModel.isStatusLike()) {
                    Log.i(LOG_TAG, "updateLike");
                    webServiceURL = webServiceURL+"updateLike";
                } else {
                    Log.i(LOG_TAG, "updateDisLike");
                    webServiceURL = webServiceURL+"updateDisLike";
                }
                callPutWebService(likeModel, webServiceURL);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private void callPostWebService(LikeModel likeModel, String webServiceURL) {
        try {
            String jsonString = new ObjectMapper().writeValueAsString(likeModel);
            AsyncHttpClient client = new AsyncHttpClient();
            client.post(getActivity(), webServiceURL, new StringEntity(jsonString, "utf-8"),
                RequestParams.APPLICATION_JSON, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        Log.i(LOG_TAG, String.format(Locale.US, "Return Status Code: %d", statusCode));
                        Log.i(LOG_TAG, "new id : "+new String(response));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        Log.e(LOG_TAG, String.format(Locale.US, "Return Status Code: %d", statusCode));
                        Log.e(LOG_TAG, "AsyncHttpClient returned error", e);
                    }
                });
        } catch (JsonProcessingException e) {
            Log.e(LOG_TAG, "JsonProcessingException : "+e.getMessage(), e);
        }
    }

    private void callPutWebService(LikeModel likeModel, String webServiceURL) {
        try {
            String jsonString = new ObjectMapper().writeValueAsString(likeModel);
            AsyncHttpClient client = new AsyncHttpClient();
            client.put(getActivity(), webServiceURL, new StringEntity(jsonString, "utf-8"),
                RequestParams.APPLICATION_JSON, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        Log.i(LOG_TAG, String.format(Locale.US, "Return Status Code: %d", statusCode));
                        Log.i(LOG_TAG, "finish updateLike");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        Log.e(LOG_TAG, String.format(Locale.US, "Return Status Code: %d", statusCode));
                        Log.e(LOG_TAG, "AsyncHttpClient returned error", e);
                    }
                });
        } catch (JsonProcessingException e) {
            Log.e(LOG_TAG, "JsonProcessingException : "+e.getMessage(), e);
        }
    }

    private LikeModel createLikeModelFromSharedPreferences() {
        LikeModel likeModel = new LikeModel();
        String like_id = sharedPref.getString("like_id", null);
        Log.i(LOG_TAG, "like_id : "+like_id);
        if(like_id != null && !"".equals(like_id)){
            likeModel.set_id(like_id);
        }
        String like_rev = sharedPref.getString("like_rev", null);
        Log.i(LOG_TAG, "like_rev : "+like_rev);
        if(like_rev != null && !"".equals(like_rev)){
            likeModel.set_rev(like_rev);
        }
        likeModel.setEmpEmail(sharedPref.getString("like_empEmail", null));
        likeModel.setTopicId(sharedPref.getString("like_topicId", null));
        likeModel.setStatusLike(sharedPref.getBoolean("like_isStatusLike", false));
        likeModel.setType(sharedPref.getString("like_type", null));
        return likeModel;
    }

    private void callGetWebService(){

        try {
            String concatGetTopicString = GET_TOPIC_URL+"?topicId="+_id+"&empEmail="+empEmail;
            Log.i(LOG_TAG,"CONCAT          :  "+concatGetTopicString);
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Cache-Control", "no-cache");
            client.get(concatGetTopicString, new BaseJsonHttpResponseHandler<List<Map>>() {

                @Override
                public void onStart() {
                    showLoadingDialog();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, List<Map> response) {
                    try {
                        topicModelMap = response;
                        room_id = (String) topicModelMap.get(0).get("roomId");
                        isLoadTopicDone = true;
                        isCommentUser(room_id);
                        setHasOptionsMenu(true);
                        Log.i(LOG_TAG, "Topic Model List Size : " + topicModelMap.size());
                        if(isLoadTopicDone && isCheckLikeDone){
                            Log.i(LOG_TAG, "finish get topic");
                            insertLikeModelToSharedPreferences();
                            generateListView();
                            closeLoadingDialog();
                        }
                    } catch (Throwable e) {
                        closeLoadingDialog();
                        Log.e(LOG_TAG, e.getMessage(), e);
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, List<Map> errorResponse) {
                    Log.e(LOG_TAG, "Error code : " + statusCode + ", " + throwable.getMessage());
                }

                @Override
                protected List<Map> parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                    Log.i(LOG_TAG, ">>>>>>>>>>>>>>>>.. Json String : "+rawJsonData);
                    return new ObjectMapper().readValue(rawJsonData,new TypeReference<List<Map<String,Object>>>() {});
                }

            });

            String concatCheckLikeString = CHECK_LIKE_URL+"?topicId="+_id+"&empEmail="+empEmail;
            client.get(concatCheckLikeString, new BaseJsonHttpResponseHandler<List<LikeModel>>() {

                @Override
                public void onStart() {
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, List<LikeModel> response) {
                    try {
                        if(response != null && !response.isEmpty()){
                            likeModel = response.get(0);
                        }
                        isCheckLikeDone = true;
                        Log.i(LOG_TAG, "LikeModel isNull : " + (response == null));
                        if(isLoadTopicDone && isCheckLikeDone){
                            Log.i(LOG_TAG, "finish get LikeModel");
                            insertLikeModelToSharedPreferences();
                            generateListView();
                            closeLoadingDialog();
                        }
                    } catch (Throwable e) {
                        closeLoadingDialog();
                        Log.e(LOG_TAG, e.getMessage(), e);
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, List<LikeModel> errorResponse) {
                    Log.e(LOG_TAG, "Error code : " + statusCode + ", " + throwable.getMessage());
                }

                @Override
                protected List<LikeModel> parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                    Log.i(LOG_TAG, ">>>>>>>>>>>>>>>>.. Json String : "+rawJsonData);
                    if("[]".equals(rawJsonData)){
                        return null;
                    } else {
                        return new ObjectMapper().readValue(rawJsonData, new TypeReference<List<LikeModel>>() {});
                    }
                }

            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "RuntimeException : "+e.getMessage(), e);
            showErrorDialog().show();
        }
    }

    private void generateListView() {
        commentListView.setAdapter(null);
        TopicAdapter commentAdapter = new TopicAdapter(getActivity(), this, topicModelMap, likeModel, canComent);
        commentListView.setAdapter(commentAdapter);
    }

    private void insertLikeModelToSharedPreferences(){
        if(likeModel == null) {
            editor.putString("like_id",  null);
            editor.putString("like_rev",  null);
            editor.putString("like_topicId",  _id);
            editor.putString("like_empEmail",  empEmail);
            editor.putBoolean("like_isStatusLike", false);
            editor.putString("like_type", "like");
            editor.commit();
        } else {
            editor.putString("like_id",  likeModel.get_id());
            editor.putString("like_rev",  likeModel.get_rev());
            editor.putString("like_topicId",  likeModel.getTopicId());
            editor.putString("like_empEmail",  likeModel.getEmpEmail());
            editor.putBoolean("like_isStatusLike",  likeModel.isStatusLike());
            editor.putString("like_type", likeModel.getType());
            editor.commit();
        }
    }

    private void showLoadingDialog() {
        progress = ProgressDialog.show(getActivity(), null,
                "Processing", true);
    }

    private void closeLoadingDialog(){
        progress.dismiss();
    }

    private AlertDialog.Builder showErrorDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage("Error while loading content.");
        alert.setCancelable(true);
        return alert;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(LOG_TAG, "onCreateOptionsMenu");
        if(canComent) {
            inflater.inflate(R.menu.board_content_menu, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    private boolean isCommentUser(String room_id) {
        boolean result = false;
        String empEmail = sharedPref.getString("empEmail", null);
        Set<String> stringSet = sharedPref.getStringSet("commentUser"+room_id, null);
        if (stringSet.contains("all") || stringSet.contains(empEmail)) {
            Log.i(LOG_TAG, "if");
            result = true;
        }
        canComent = result;
        Log.i(LOG_TAG, "isCommentUser : "+canComent);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnComment:
                callWritingCommentFragment();
                return true;
            default:
                break;
        }

        return false;
    }

    private void callWritingCommentFragment() {
        Log.i(LOG_TAG, "click comment");
        Bundle data = new Bundle();
        data.putString("_id", _id);
        data.putString("room_id", room_id);
        Fragment fragment = new WritingCommentFragment();
        fragment.setArguments(data);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("tag").addToBackStack(null).commit();
    }

    @Override
    public void onDataPass(String data) {
        if(data.equals("comment")){
            callWritingCommentFragment();
        }else if(data.equals("refresh")){
            callGetWebService();
        }

    }
}
