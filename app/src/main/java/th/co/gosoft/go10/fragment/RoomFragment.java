package th.co.gosoft.go10.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import th.co.gosoft.go10.R;
import th.co.gosoft.go10.activity.WritingTopicActivity;
import th.co.gosoft.go10.adapter.HostTopicListAdapter;
import th.co.gosoft.go10.model.TopicModel;

public class RoomFragment extends Fragment {

    private final String LOG_TAG = "RoomFragment_Tag";
    private final String URL = "http://go10webservice.au-syd.mybluemix.net/GO10WebService/api/topic/gettopiclistbyroom";
    private ProgressDialog progress;
    private Map<String, Integer> imageIdMap = new HashMap<>();
    private List<TopicModel> topicModelList = new ArrayList<>();

    private String room_id;
    private String roomName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "Oncreate : RoomFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.activity_room, container, false);
        Bundle bundle = getArguments();
        room_id = bundle.getString("room_id");
        roomName = bundle.getString("roomName");
        Log.i(LOG_TAG, "room_id : " + room_id);
        Log.i(LOG_TAG, "roomName : " + roomName);

        generateImageToMap(imageIdMap);
        ImageView imageView = (ImageView) view.findViewById(R.id.roomIcon);
        int id = getResources().getIdentifier(String.valueOf(imageIdMap.get(room_id)), null, null);
        Log.i(LOG_TAG, "id img : " + id);
        imageView.setImageResource(id);
        TextView txtRoomName = (TextView)  view.findViewById(R.id.txtRoomName);
        txtRoomName.setText(roomName);

        Button button = (Button) view.findViewById(R.id.btnNewTopic);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), WritingTopicActivity.class);
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("room_id", room_id);
                startActivity(intent);
//            Bundle data = new Bundle();
//            data.putString("room_id", room_id);
//            Fragment fragment = new WritingTopicFragment();
//            fragment.setArguments(data);
//            FragmentManager fragmentManager = getFragmentManager();
//            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();

            }
        });
            return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        ListView topicListView = (ListView)  getView().findViewById(R.id.listViewTopic);
        topicListView.setAdapter(null);
        callGetWebService();
    }

    private void callGetWebService(){
        String concatString = URL+"?roomId="+room_id;
        Log.i(LOG_TAG, "URL : " + concatString);
        try {
            AsyncHttpClient client = new AsyncHttpClient();
            Log.i(LOG_TAG, "client");
            client.get(concatString, new BaseJsonHttpResponseHandler<List<TopicModel>>() {

                @Override
                public void onStart() {
                    showLoadingDialog();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, List<TopicModel> response) {
                    try {
                        Log.i(LOG_TAG, "raw json : " + rawJsonResponse);
                        topicModelList = response;
                        generateListView();
                        closeLoadingDialog();
                        Log.i(LOG_TAG, "Topic Model List Size : " + topicModelList.size());

                    } catch (Throwable e) {
                        closeLoadingDialog();
                        Log.e(LOG_TAG, e.getMessage(), e);
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, List<TopicModel> errorResponse) {
                    Log.e(LOG_TAG, "Error code : " + statusCode + ", " + throwable.getMessage());
                }

                @Override
                protected List<TopicModel> parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                    Log.i(LOG_TAG, ">>>>>>>>>>>>>>>>.. Json String : " + rawJsonData);
                    return new ObjectMapper().readValue(rawJsonData, new TypeReference<List<TopicModel>>() {
                    });
                }

            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "RuntimeException : "+e.getMessage(), e);
            showErrorDialog().show();
        }
    }

    private void generateListView() {
        ListView topicListView = (ListView)  getView().findViewById(R.id.listViewTopic);
        HostTopicListAdapter hostTopicListAdapter = new HostTopicListAdapter(getActivity(), R.layout.hot_topic_row, topicModelList);
        topicListView.setAdapter(hostTopicListAdapter);

        topicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                goBoardContentActivity(position);
            }
        });
    }

    private void goBoardContentActivity(int position) {
        Bundle data = new Bundle();
        data.putString("_id", topicModelList.get(position).get_id());
        Fragment fragment = new BoardContentFragment();
        fragment.setArguments(data);
        FragmentManager fragmentManager = getFragmentManager();
//            FragmentTransaction tx = fragmentManager.beginTransaction();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
    }

    private void generateImageToMap(Map<String, Integer> imageIdMap) {
        imageIdMap.put("rm01", R.drawable.general);
        imageIdMap.put("rm02", R.drawable.it_knowledge);
        imageIdMap.put("rm03", R.drawable.sport);
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
        alert.setMessage("Error Occurred!!!");
        alert.setCancelable(true);
        return alert;
    }

}