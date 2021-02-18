package co.kr.sumai;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WebViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebViewFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; //웹뷰세팅
    private String url;

    public WebViewFragment() {
        // Required empty public constructor
    }

    public WebViewFragment(String url) {
        this.url = url;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WebViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WebViewFragment newInstance(String param1, String param2) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_web_view, container, false);

        if (url != null) {
            mWebView = (WebView) v.findViewById(R.id.termsWebView);

            mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
            mWebSettings = mWebView.getSettings(); //세부 세팅 등록
            mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
            mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
            mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
            mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
            mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
            mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
            mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
            mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
            mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부
            mWebView.setVerticalScrollBarEnabled(false);

            mWebView.loadUrl(url);  //웹뷰 실행
            mWebView.setWebChromeClient(new WebChromeClient());  //웹뷰에 크롬 사용 허용//이 부분이 없으면 크롬에서 alert가 뜨지 않음
            mWebView.setWebViewClient(new WebViewClientClass());  //새창열기 없이 웹뷰 내에서 다시 열기//페이지 이동 원활히 하기위해 사용
        }

        // Inflate the layout for this fragment
        return v;
    }
    private class WebViewClientClass extends WebViewClient {//페이지 이동
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}