package co.kr.sumai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_web_view.*

/**
 * A simple [Fragment] subclass.
 * Use the [WebViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WebViewFragment : Fragment {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var url: String? = null

    constructor() {
        // Required empty public constructor
    }

    constructor(url: String?) {
        this.url = url
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_web_view, container, false)
        if (url != null) {
            val mWebView: WebView = v.findViewById(R.id.termsWebView)
            mWebView.webViewClient = WebViewClient() // 클릭시 새창 안뜨게
            val mWebSettings = mWebView.settings //세부 세팅 등록
            mWebSettings.javaScriptEnabled = true // 웹페이지 자바스클비트 허용 여부
            mWebSettings.setSupportMultipleWindows(false) // 새창 띄우기 허용 여부
            mWebSettings.javaScriptCanOpenWindowsAutomatically = false // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
            mWebSettings.loadWithOverviewMode = true // 메타태그 허용 여부
            mWebSettings.useWideViewPort = true // 화면 사이즈 맞추기 허용 여부
            mWebSettings.setSupportZoom(false) // 화면 줌 허용 여부
            mWebSettings.builtInZoomControls = false // 화면 확대 축소 허용 여부
            mWebSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN // 컨텐츠 사이즈 맞추기
            mWebSettings.cacheMode = WebSettings.LOAD_NO_CACHE // 브라우저 캐시 허용 여부
            mWebSettings.domStorageEnabled = true // 로컬저장소 허용 여부
            mWebView.isVerticalScrollBarEnabled = false
            mWebView.loadUrl(url) //웹뷰 실행
            mWebView.webChromeClient = WebChromeClient() //웹뷰에 크롬 사용 허용//이 부분이 없으면 크롬에서 alert가 뜨지 않음
            mWebView.webViewClient = WebViewClientClass() //새창열기 없이 웹뷰 내에서 다시 열기//페이지 이동 원활히 하기위해 사용
        }

        // Inflate the layout for this fragment
        return v
    }

    private inner class WebViewClientClass : WebViewClient() {
        //페이지 이동
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WebViewFragment.
         */
        fun newInstance(param1: String?, param2: String?): WebViewFragment {
            val fragment = WebViewFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}