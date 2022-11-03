package com.mytaw.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebActivity extends AppCompatActivity implements View.OnClickListener {

    private WebView webView;
    private ProgressBar progressBar;
    private EditText textUrl;
    private ImageView webIcon, goBack, goForward, navSet, goHome, btnStart;

    private long exitTime = 0;

    private Context mContext;
    private InputMethodManager manager;

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final int PRESS_BACK_EXIT_GAP = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Alt Düğmenin Yukarı Hareket Etmesini Engelle
        getWindow().setSoftInputMode
                (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_web);

        mContext = WebActivity.this;
        manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // Bağlama Kontrolleri
        initView();

        // Web Görünümünü Başlat
        initWeb();
    }

    /**
     * Bağlama Kontrolleri
     */
    private void initView() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        textUrl = findViewById(R.id.textUrl);
        webIcon = findViewById(R.id.webIcon);
        btnStart = findViewById(R.id.btnStart);
        goBack = findViewById(R.id.goBack);
        goForward = findViewById(R.id.goForward);
        navSet = findViewById(R.id.navSet);
        goHome = findViewById(R.id.goHome);

        // Bağlama Düğmesi Tıklama Olayı
        btnStart.setOnClickListener(this);
        goBack.setOnClickListener(this);
        goForward.setOnClickListener(this);
        navSet.setOnClickListener(this);
        goHome.setOnClickListener(this);

        // Adres Giriş Alanı Odağı Alır Ve Kaybeder
        textUrl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    // Geçerli URL Bağlantısını Göster TODO:Arama Terimlerini Gösteren Arama Sayfası
                    textUrl.setText(webView.getUrl());
                    // Sonunda İmleç
                    textUrl.setSelection(textUrl.getText().length());
                    // İnternet Simgesini Göster
                    webIcon.setImageResource(R.drawable.internet);
                    // Atlama düğmelerini göster
                    btnStart.setImageResource(R.drawable.go);
                } else {
                    // Web Sitesi Adını Göster
                    textUrl.setText(webView.getTitle());
                    // Favicon'u Göster
                    webIcon.setImageBitmap(webView.getFavicon());
                    // Yenile Düğmesini Göster
                    btnStart.setImageResource(R.drawable.refresh);
                }
            }
        });

        // Monitör Klavyesi Aramaya Gir
        textUrl.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    // Arama Yap
                    btnStart.callOnClick();
                    textUrl.clearFocus();
                }
                return false;
            }
        });
    }


    /**
     * Web'i Başlat
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWeb() {
        // Yeniden Yazmak WebViewClient
        webView.setWebViewClient(new TawWebViewClient());
        // Yeniden Yazmak WebChromeClient
        webView.setWebChromeClient(new TawWebChromeClient());

        WebSettings settings = webView.getSettings();
        // JS İşlevini Etkinleştir
        settings.setJavaScriptEnabled(true);
        // Tarayıcıyı Kullanıcı Aracısı Olarak Ayarla
        settings.setUserAgentString(settings.getUserAgentString() + " TawBrowser/" + getVerName(mContext));

        // Resmi Web Görünümüne Sığdırmak İçin Yeniden Boyutlandırın
        settings.setUseWideViewPort(true);
        // Ekranın Boyutuna Yakınlaştırın
        settings.setLoadWithOverviewMode(true);

        // Ölçeklendirmeyi Destekler, Varsayılanlar true'dur.  Aşağıdakilerin Öncülüdür.
        settings.setSupportZoom(true);
        // Yerleşik Yakınlaştırma Kontrollerini Ayarlar.  false'yse, WebView Yakınlaştırılamaz
        settings.setBuiltInZoomControls(true);
        // Yerel Yakınlaştırma Denetimlerini Gizle
        settings.setDisplayZoomControls(false);

        // Önbellek
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // Dosya Erişimini Ayarla
        settings.setAllowFileAccess(true);
        // JS Aracılığıyla Yeni Pencerelerin Açılmasını Destekleyin
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // Resimlerin Otomatik Yüklenmesini Destekleyin
        settings.setLoadsImagesAutomatically(true);
        // Varsayılan Kodlama Biçimini Ayarlayın
        settings.setDefaultTextEncodingName("utf-8");
        // Yerel Depolama
        settings.setDomStorageEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);

        // Kaynak Karışımı Modu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // Ana Sayfayı Yükle
        webView.loadUrl(getResources().getString(R.string.home_url));
    }


    /**
     * Yeniden Yazmak WebViewClient
     */
    private class TawWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // WebView'e Tıklayarak Açılan Yeni Web Sayfasını, Yeni Tarayıcıya Atlamadan Mevcut Arayüzde Görüntülenecek Şekilde Ayarlayın

            if (url == null) {
                // Kendiniz Halletmek İçin true, İşlememek İçin false Döndürün
                return true;
            }

            // Normal İçerik, Açık
            if (url.startsWith(HTTP) || url.startsWith(HTTPS)) {
                view.loadUrl(url);
                return true;
            }

            // Üçüncü Taraf Uygulamaları Arayın，Engel Olmak Crash (Telefonunuzda Yüklü Bir Cihaz Yoksa个scheme Başlangıç url的APP, 会导致crash)
            try {
                // TODO:Pop-up Kullanıcıya Sorar，İzin Aldıktan Sonra Ara
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            } catch (Exception e) {
                return true;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            // Bir İlerleme Çubuğu Gösteren Web Sayfası Yüklenmeye Başlar
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);

            // Durum Metnini Güncelle
            textUrl.setText("Yükleniyor...");

            // Varsayılan Web Simgesini Aç/Kapat
            webIcon.setImageResource(R.drawable.internet);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // Sayfa Yüklendi Ve İlerleme Çubuğu Gizlendi
            progressBar.setVisibility(View.INVISIBLE);

            // Başlığı Değiştir
            setTitle(webView.getTitle());
            // Sayfa Başlığını Göster
            textUrl.setText(webView.getTitle());
        }
    }


    /**
     * Yeniden Yazmak WebChromeClient
     */
    private class TawWebChromeClient extends WebChromeClient {
        private final static int WEB_PROGRESS_MAX = 100;

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);

            // İlerleme Değişiklikleri Yükleniyor, İlerleme Çubuğunu Yenileyin
            progressBar.setProgress(newProgress);
            if (newProgress > 0) {
                if (newProgress == WEB_PROGRESS_MAX) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);

            // Simgeyi Değiştir
            webIcon.setImageBitmap(icon);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);

            // Başlığı Değiştir
            setTitle(title);
            // Sayfa Başlığını Göster
            textUrl.setText(title);
        }
    }

    /**
     * Geri Düğmesi Kullanımı
     */
    @Override
    public void onBackPressed() {
        // Geri Dönebiliyorsanız, Önceki Sayfaya Dönün
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if ((System.currentTimeMillis() - exitTime) > PRESS_BACK_EXIT_GAP) {
                // Programdan Çıkmak İçin Çift Tıklayın
                Toast.makeText(mContext, "Programdan çıkmak için tekrar basın",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Atla Veya Yenile
            case R.id.btnStart:
                if (textUrl.hasFocus()) {
                    // Yumuşak Klavyeyi Gizle
                    if (manager.isActive()) {
                        manager.hideSoftInputFromWindow(textUrl.getApplicationWindowToken(), 0);
                    }

                    // Adres Çubuğunun Odağı Var, Bu Bir Sıçrama
                    String input = textUrl.getText().toString();
                    if (!isHttpUrl(input)) {
                        // URL Değil, İşlenmek Üzere Arama Motoru Tarafından Yüklendi
                        try {
                            // URL Kodlama
                            input = URLEncoder.encode(input, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        input = "https://www.google.com/search?q=" + input + "&ie=UTF-8";
                    }
                    webView.loadUrl(input);

                    // Adres Çubuğunun Odağını İptal Et
                    textUrl.clearFocus();
                } else {
                    // Adres Çubuğunda Odak Yok, Yenileniyor
                    webView.reload();
                }
                break;

            // Geri
            case R.id.goBack:
                webView.goBack();
                break;

            // İleri
            case R.id.goForward:
                webView.goForward();
                break;

            // Ayarlar
            case R.id.navSet:
                Toast.makeText(mContext, "Bu sürüm geliştirme sürümüdür. desteklenmiyor", Toast.LENGTH_SHORT).show();
                break;

            // Anasayfa
            case R.id.goHome:
                webView.loadUrl(getResources().getString(R.string.home_url));
                break;

            default:
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            webView.getClass().getMethod("onPause").invoke(webView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            webView.getClass().getMethod("onResume").invoke(webView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Bir Dize Olup Olmadığını BelirleyinURL
     *
     * @param urls Eşlemek İçin Dize
     * @return true:EvetURL、false:HayırURL
     */
    public static boolean isHttpUrl(String urls) {
        boolean isUrl;
        // URL Olup Olmadığını Belirlemek İçin Normal İfade
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";

        Pattern pat = Pattern.compile(regex.trim());
        Matcher mat = pat.matcher(urls.trim());
        isUrl = mat.matches();
        return isUrl;
    }

    /**
     * Sürüm Numarası Adını Al
     *
     * @param context Bağlam
     * @return Mevcut Sürüm Adı
     */
    private static String getVerName(Context context) {
        String verName = "unKnow";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }
}
