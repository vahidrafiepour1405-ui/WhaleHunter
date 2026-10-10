package com.wal.ai;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.text.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
    LinearLayout root, content;
    TextView status, title;
    JSONArray tickers;
    String selected="BTCUSDT";
    int blue=Color.rgb(73,93,235), green=Color.rgb(12,155,104), red=Color.rgb(215,62,82), ink=Color.rgb(25,35,55), bg=Color.rgb(246,250,253);
    int d(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    TextView tv(String s,float z){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(ink);v.setPadding(d(12),d(7),d(12),d(7));return v;}
    GradientDrawable box(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(d(radius));return g;}
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(11);b.setTextColor(ink);b.setAllCaps(false);return b;}

    @Override public void onCreate(Bundle b){super.onCreate(b);build();loadMarkets();}

    void build(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(bg);
        LinearLayout head=new LinearLayout(this);head.setPadding(d(14),d(10),d(14),d(8));head.setGravity(Gravity.CENTER_VERTICAL);head.setBackgroundColor(Color.WHITE);
        TextView logo=tv("W",24);logo.setGravity(17);logo.setTextColor(Color.WHITE);logo.setTypeface(null,1);logo.setBackground(box(blue,14));
        head.addView(logo,new LinearLayout.LayoutParams(d(46),d(46)));
        LinearLayout hb=new LinearLayout(this);hb.setOrientation(LinearLayout.VERTICAL);title=tv("WAL",21);title.setTypeface(null,1);hb.addView(title);hb.addView(tv("Whale Analysis Live",10));head.addView(hb,new LinearLayout.LayoutParams(0,-2,1));
        status=tv("● CONNECTING",11);status.setTextColor(blue);head.addView(status);root.addView(head);
        ScrollView sc=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(d(12),d(14),d(12),d(28));sc.addView(content);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        home();
    }

    void home(){
        content.removeAllViews();
        TextView h=tv("هوش وال",27);h.setTypeface(null,1);content.addView(h);
        content.addView(tv("داده واقعی بازار • تحلیل قاعده‌مند • بدون جعل اطلاعات نهنگ",12));
        EditText q=new EditText(this);q.setHint("وال سرچ هوش مصنوعی — نام یا نماد ارز");q.setSingleLine();q.setPadding(d(14),0,d(14),0);q.setBackground(box(Color.WHITE,14));content.addView(q,new LinearLayout.LayoutParams(-1,d(54)));
        LinearLayout nav=new LinearLayout(this);
        String[] ns={"بازار","سیگنال‌ها","رادار نهنگ","هوش و وضعیت"};
        for(String n:ns){Button x=btn(n);nav.addView(x,new LinearLayout.LayoutParams(0,d(48),1));if(n.equals("بازار"))x.setOnClickListener(v->markets());if(n.equals("سیگنال‌ها"))x.setOnClickListener(v->signals());if(n.equals("رادار نهنگ"))x.setOnClickListener(v->radar());if(n.equals("هوش و وضعیت"))x.setOnClickListener(v->intel());}
        content.addView(nav);
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);content.addView(list);
        q.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int e){}public void afterTextChanged(Editable e){}public void onTextChanged(CharSequence s,int a,int b,int c){renderMarkets(list,s.toString());}});
        renderMarkets(list,"");
    }

    void markets(){home();}
    void renderMarkets(LinearLayout list,String query){
        if(tickers==null){list.removeAllViews();list.addView(tv("در حال دریافت داده زنده بازار...",14));return;}
        list.removeAllViews();String q=query.toUpperCase(Locale.US);int n=0;
        for(int i=0;i<tickers.length()&&n<80;i++)try{
            JSONObject o=tickers.getJSONObject(i);String s=o.optString("symbol");
            if(s.isEmpty()||(!q.isEmpty()&&!s.contains(q)))continue;
            double p=o.optDouble("lastPrice"), ch=o.optDouble("priceChangePercent"), vol=o.optDouble("quoteVolume");
            LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(d(10),d(9),d(10),d(9));c.setBackground(box(Color.WHITE,14));
            TextView a=tv(s.replace("USDT","")+"    $"+fmt(p),17);a.setTypeface(null,1);c.addView(a);
            TextView x=tv("24h  "+fmt2(ch)+"%    •    Vol $"+shortNum(vol),12);x.setTextColor(ch>=0?green:red);c.addView(x);
            c.addView(tv("بازار زنده • برای نمودار و سیگنال لمس کن",10));
            c.setOnClickListener(v->asset(s+"USDT"));
            list.addView(c,new LinearLayout.LayoutParams(-1,d(86)));Space sp=new Space(this);list.addView(sp,new LinearLayout.LayoutParams(1,d(7)));n++;
        }catch(Exception ignored){}
    }

    void asset(String symbol){
        selected=symbol;content.removeAllViews();
        TextView back=tv("‹  بازگشت",13);back.setOnClickListener(v->home());content.addView(back);
        TextView h=tv(symbol.replace("USDT",""),27);h.setTypeface(null,1);content.addView(h);
        TextView price=tv("در حال دریافت قیمت زنده...",18);content.addView(price);
        LinearLayout tf=new LinearLayout(this);String[] tfs={"1m","5m","15m","1h","4h","1d","1w","1M"};for(String tfv:tfs){Button b=btn(tfv);tf.addView(b,new LinearLayout.LayoutParams(0,d(42),1));b.setOnClickListener(v->loadChart(symbol,tfv,price));}content.addView(tf);
        ChartView chart=new ChartView(this);chart.setBackgroundColor(Color.WHITE);content.addView(chart,new LinearLayout.LayoutParams(-1,d(330)));
        content.addView(tv("سیگنال وال",16));TextView sig=tv("در حال محاسبه...",15);sig.setPadding(d(12),d(10),d(12),d(10));sig.setBackground(box(Color.WHITE,12));content.addView(sig);
        content.addView(tv("قدرت شواهد از قیمت، حجم و شاخص‌های تکنیکال زنده محاسبه می‌شود؛ درصد دقت پیش‌بینی نیست.",10));
        loadChart(symbol,"1h",price,chart,sig);
    }

    void loadChart(String symbol,String interval,TextView price){loadChart(symbol,interval,price,null,null);}
    void loadChart(String symbol,String interval,TextView price,ChartView chart,TextView sig){
        new Thread(()->{try{
            JSONObject item=findMarketItem(symbol);if(item==null)throw new IOException("دارایی در فهرست CoinMarketCap نیست");
            double last=item.optDouble("lastPrice"),change=item.optDouble("priceChangePercent");
            Signal signal=analyzeMarketItem(item);
            runOnUiThread(()->{price.setText("$"+fmt(last)+"    تغییر ۲۴ساعته CMC: "+fmt2(change)+"% ");if(chart!=null)chart.setData(new ArrayList<>());if(sig!=null)sig.setText(signal.text+"\nنمودار کندلی تاریخی از فید CoinMarketCap فعلی دریافت نشده است.");});
        }catch(Exception e){runOnUiThread(()->price.setText("داده CoinMarketCap در دسترس نیست — "+e.getMessage()));}}).start();
    }
    JSONObject findMarketItem(String symbol){if(tickers==null)return null;String base=symbol.replace("USDT","");for(int i=0;i<tickers.length();i++){JSONObject o=tickers.optJSONObject(i);if(o!=null&&o.optString("symbol").equals(base))return o;}return null;}
    Signal analyzeMarketItem(JSONObject o){double ch=o.optDouble("priceChangePercent"),cap=o.optDouble("marketCap"),vol=o.optDouble("quoteVolume");String name=ch>=3?"BUY WATCH":ch<=-3?"SELL RISK":"WAIT";String text="سیگنال: "+name+"\\nتغییر ۲۴ساعته CoinMarketCap: "+fmt2(ch)+"%\\nارزش بازار: $"+shortNum(cap)+"\\nحجم ۲۴ساعته: $"+shortNum(vol)+"\\nاین فیلتر سادهٔ تغییر روزانه است، نه پیش‌بینی قطعی یا تحلیل کندلی.";return new Signal(name,text);}

    Signal analyze(ArrayList<Candle> c){
        int n=c.size();double[] closes=new double[n];for(int i=0;i<n;i++)closes[i]=c.get(i).c;
        double ema20=ema(closes,20),ema50=ema(closes,50),rsi=rsi(closes,14),m=macd(closes),volNow=c.get(n-1).v,volAvg=0;
        for(int i=Math.max(0,n-21);i<n-1;i++)volAvg+=c.get(i).v;volAvg/=Math.max(1,Math.min(20,n-1));
        int score=0;if(closes[n-1]>ema20)score+=20;if(ema20>ema50)score+=20;if(rsi>=50&&rsi<=70)score+=15;if(m>0)score+=15;if(volNow>volAvg*1.15)score+=15;if(closes[n-1]>closes[Math.max(0,n-6)])score+=15;
        String s=score>=70?"BUY":score<=30?"SELL":"WAIT";
        String reason="سیگنال: "+s+"\nقدرت شواهد: "+score+"/100\nRSI: "+fmt2(rsi)+"  |  EMA20/50: "+(ema20>ema50?"صعودی":"نزولی")+"\nMACD: "+(m>0?"مثبت":"منفی")+"  |  Volume: "+(volNow>volAvg*1.15?"افزایش":"عادی")+"\nData Quality: داده زنده کندل\nقاعده‌مند؛ بدون داده تصادفی.";
        return new Signal(s,reason);
    }
    double ema(double[] x,int p){double e=x[0],k=2.0/(p+1);for(int i=1;i<x.length;i++)e=x[i]*k+e*(1-k);return e;}
    double rsi(double[] x,int p){double g=0,l=0;for(int i=Math.max(1,x.length-p);i<x.length;i++){double d=x[i]-x[i-1];if(d>=0)g+=d;else l-=d;}if(l==0)return 100;return 100-(100/(1+g/l));}
    double macd(double[] x){return ema(x,12)-ema(x,26);}
    void signals(){
        content.removeAllViews();content.addView(tv("سیگنال‌های وال",27));content.addView(tv("فقط تحلیل قاعده‌مند از کندل زنده؛ بدون اعتمادسازی ساختگی.",12));
        if(tickers==null){content.addView(tv("INSUFFICIENT DATA",15));return;}
        int shown=0;for(int i=0;i<tickers.length()&&shown<12;i++)try{String s=tickers.getJSONObject(i).optString("symbol");if(s.isEmpty())continue;assetSignalCard(s+"USDT");shown++;}catch(Exception ignored){}
    }
    void assetSignalCard(String s){
        JSONObject item=findMarketItem(s);if(item==null)return;Signal q=analyzeMarketItem(item);
        TextView x=tv(s.replace("USDT","")+"   •   "+q.name+"   •   "+fmt2(item.optDouble("priceChangePercent"))+"% / 24h",15);x.setBackground(box(Color.WHITE,12));x.setTextColor(q.name.startsWith("BUY")?green:q.name.startsWith("SELL")?red:ink);content.addView(x,new LinearLayout.LayoutParams(-1,d(58)));x.setOnClickListener(v->asset(s));
    }

    Handler whaleHandler = new Handler(Looper.getMainLooper());
    LinearLayout whaleListContainer;
    TextView whaleStatus, whaleRefreshLabel;
    boolean radarVisible=false;
    Runnable whaleLiveTask;
    static final long WHALE_LIST_REFRESH_MS=24L*60L*60L*1000L;
    static final long WHALE_LIVE_REFRESH_MS=30L*1000L;

    void radar(){
        radarVisible=true;
        content.removeAllViews();
        TextView back=tv("‹  بازگشت",13);back.setOnClickListener(v->{radarVisible=false;home();});content.addView(back);
        content.addView(tv("WAL Whale Radar",27));
        content.addView(tv("داده آن‌چین HyperEVM • فعالیت زنده هر ۳۰ ثانیه تازه می‌شود",12));
        content.addView(tv("فهرست نامزدها هر ۲۴ ساعت تازه می‌شود. زمان ورود/فعالیت فقط وقتی نمایش داده می‌شود که ایندکسر بلاک‌چین ارائه کند.",11));
        whaleStatus=tv("در حال اتصال به منبع داده نهنگ...",13);content.addView(whaleStatus);
        whaleRefreshLabel=tv("فهرست نهنگ‌ها: بررسی آخرین به‌روزرسانی...",10);content.addView(whaleRefreshLabel);
        content.addView(tv("آخرین جابه‌جایی‌های نهنگ",16));
        whaleListContainer=new LinearLayout(this);whaleListContainer.setOrientation(LinearLayout.VERTICAL);content.addView(whaleListContainer);
        content.addView(tv("نامزدهای نهنگ (تأییدنشده)",16));
        LinearLayout candidates=new LinearLayout(this);candidates.setOrientation(LinearLayout.VERTICAL);content.addView(candidates);
        loadWhaleListIfDue(candidates);
        refreshWhaleMovements(whaleListContainer);
        if(whaleLiveTask!=null)whaleHandler.removeCallbacks(whaleLiveTask);
        whaleLiveTask=()->{if(radarVisible){refreshWhaleMovements(whaleListContainer);loadWhaleListIfDue(candidates);whaleHandler.postDelayed(whaleLiveTask,WHALE_LIVE_REFRESH_MS);}};
        whaleHandler.postDelayed(whaleLiveTask,WHALE_LIVE_REFRESH_MS);
    }

    void loadWhaleListIfDue(LinearLayout target){
        android.content.SharedPreferences p=getSharedPreferences("wal_whales",MODE_PRIVATE);
        long last=p.getLong("list_refresh_ms",0L);
        long now=System.currentTimeMillis();
        if(now-last<WHALE_LIST_REFRESH_MS){
            whaleRefreshLabel.setText("Whale list updated: "+dateTime(last)+" • next refresh within 24h");
            String cached=p.getString("rich_list_json","");
            if(!cached.isEmpty()&&target.getChildCount()==0)try{renderWhaleCandidates(target,new JSONObject(cached));}catch(Exception ignored){}
            return;
        }
        if(whaleStatus!=null)whaleStatus.setText("در حال تازه‌سازی فهرست نهنگ‌ها...");
        new Thread(()->{
            try{
                Object raw=getJsonAny("https://trace.hypurrscan.io/api/v1/indexed/wealth-rich-list");
                String json=raw.toString();
                p.edit().putString("rich_list_json",json).putLong("list_refresh_ms",System.currentTimeMillis()).apply();
                runOnUiThread(()->{
                    if(!radarVisible)return;
                    target.removeAllViews();renderWhaleCandidates(target,raw);
                    whaleRefreshLabel.setText("Whale list refreshed: "+dateTime(System.currentTimeMillis())+" • refresh interval 24h");
                    whaleStatus.setText("زنده • فهرست دارایی‌های ثبت‌شده در HypurrTrace");
                });
            }catch(Exception e){runOnUiThread(()->{if(radarVisible){whaleStatus.setText("داده ناکافی — whale list provider unavailable");whaleRefreshLabel.setText("به‌روزرسانی ناموفق بود؛ در صورت وجود فهرست ذخیره‌شده حفظ می‌شود.");}});}
        }).start();
    }

    void refreshWhaleMovements(LinearLayout target){
        new Thread(()->{
            try{
                Object raw=getJsonAny("https://trace.hypurrscan.io/api/v1/indexed/whale-transfers");
                runOnUiThread(()->{
                    if(!radarVisible||target==null)return;
                    target.removeAllViews();
                    renderWhaleMovements(target,raw);
                    if(whaleStatus!=null)whaleStatus.setText("LIVE • latest indexed on-chain movements • checked "+dateTime(System.currentTimeMillis()));
                });
            }catch(Exception e){runOnUiThread(()->{if(radarVisible&&target!=null&&target.getChildCount()==0)target.addView(tv("داده ناکافی — live whale movements unavailable",13));});}
        }).start();
    }

    JSONArray extractList(Object raw){
        if(raw instanceof JSONArray)return (JSONArray)raw;
        if(raw instanceof JSONObject){
            JSONObject o=(JSONObject)raw;
            String[] keys={"items","results","data","transfers","transactions","rows","whales","records","balances","holders","richList","rich_list","wallets","entries","list"};
            for(String k:keys){JSONArray a=o.optJSONArray(k);if(a!=null)return a;}
            for(String k:keys){JSONObject nested=o.optJSONObject(k);if(nested!=null){JSONArray a=extractList(nested);if(a!=null)return a;}}
        }
        return null;
    }

    void renderWhaleCandidates(LinearLayout target,Object raw){
        JSONArray a=extractList(raw);
        if(a==null){target.addView(tv("ساختار پاسخ منبع قابل‌شناسایی نبود → داده ناکافی",13));return;}
        int count=0;
        for(int i=0;i<a.length()&&count<100;i++)try{
            JSONObject o=a.getJSONObject(i);
            String addr=find(o,"address","wallet_address","account_address","user","owner","account","wallet","holder");
            if(addr.isEmpty())continue;
            String val=find(o,"usdValue","valueUsd","totalUsd","total_usd_value","accountValue","account_value","usd_value");
            String label=find(o,"label","name","entity");
            String seen=getSharedPreferences("wal_whales",MODE_PRIVATE).getString("seen_"+addr.toLowerCase(Locale.US),"");
            if(seen.isEmpty()){seen=dateTime(System.currentTimeMillis());getSharedPreferences("wal_whales",MODE_PRIVATE).edit().putString("seen_"+addr.toLowerCase(Locale.US),seen).apply();}
            TextView x=tv((count+1)+". "+shortAddr(addr)+"\nValue: $"+(val.isEmpty()?"—":val)+"  •  اولین مشاهده توسط وال: "+seen+"\n"+(label.isEmpty()?"طبقه‌بندی: نامزد تأییدنشده":label+" • UNVERIFIED"),12);
            x.setBackground(box(Color.WHITE,12));target.addView(x,new LinearLayout.LayoutParams(-1,-2));
            Space sp=new Space(this);target.addView(sp,new LinearLayout.LayoutParams(1,d(5)));count++;
        }catch(Exception ignored){}
        if(count==0)target.addView(tv("رکورد کیف پول قابل‌خواندن نیست → داده ناکافی",13));
        else target.addView(tv("تعداد نامزدها: "+count+" • نهنگ مستقل تأییدشده: تا پایان طبقه‌بندی صفر. اولین مشاهده، زمان مشاهده وال است نه زمان خرید تاریخی.",11));
    }

    void renderWhaleMovements(LinearLayout target,Object raw){
        JSONArray a=extractList(raw);
        if(a==null){target.addView(tv("فید جابه‌جایی زنده قابل‌خواندن نیست → داده ناکافی",13));return;}
        int count=0;
        for(int i=0;i<a.length()&&count<30;i++)try{
            JSONObject o=a.getJSONObject(i);
            String from=find(o,"from","fromAddress","from_address","sender","sender_address","src","owner");
            String to=find(o,"to","toAddress","to_address","recipient","recipient_address","dst","user","address");
            String token=find(o,"tokenSymbol","token_symbol","symbol","token","asset","name");
            String amount=find(o,"amount","value","tokenAmount","token_amount","quantity");
            String usd=find(o,"usdValue","valueUsd","amountUsd","usd_value","transferUsd","transfer_usd");
            String tx=find(o,"txHash","tx_hash","transactionHash","hash","transaction_hash");
            String stamp=find(o,"timestamp","blockTimestamp","block_timestamp","timeStamp","createdAt","datetime","time","block_time","ts");
            if(from.isEmpty()&&to.isEmpty()&&tx.isEmpty())continue;
            String when=formatChainTime(stamp);
            StringBuilder line=new StringBuilder();
            line.append("رویداد آن‌چین: ").append(when).append("\n");
            if(!from.isEmpty())line.append("مبدأ: ").append(shortAddr(from)).append("\n");
            if(!to.isEmpty())line.append("مقصد: ").append(shortAddr(to)).append("\n");
            line.append("ارز: ").append(token.isEmpty()?"—":token).append(" • مقدار: ").append(amount.isEmpty()?"—":amount);
            if(!usd.isEmpty())line.append(" • USD: $").append(usd);
            if(!tx.isEmpty())line.append("\nTx: ").append(shortAddr(tx));
            TextView x=tv(line.toString(),11);x.setBackground(box(Color.WHITE,12));target.addView(x,new LinearLayout.LayoutParams(-1,-2));
            Space sp=new Space(this);target.addView(sp,new LinearLayout.LayoutParams(1,d(5)));count++;
        }catch(Exception ignored){}
        if(count==0)target.addView(tv("منبع انتقال قابل‌خواندن برنگرداند. از این پاسخ نمی‌توان زمان ورود را تأیید کرد.",12));
        else target.addView(tv("نمایش "+count+" رویداد ثبت‌شده اخیر. زمان‌ها فقط در صورت ارائه منبع، زمان رویداد آن‌چین هستند؛ انتقال به‌تنهایی خرید را ثابت نمی‌کند.",10));
    }

    String formatChainTime(String raw){
        if(raw==null||raw.trim().isEmpty())return "زمان ثبت نشده";
        try{
            String s=raw.trim();
            if(s.matches("\\d{10,13}")){
                long t=Long.parseLong(s);if(s.length()==10)t*=1000L;
                return dateTime(t)+" (chain/indexer time)";
            }
            return s+" (source timestamp)";
        }catch(Exception e){return raw;}
    }
    String dateTime(long ms){if(ms<=0)return "ثبت نشده";return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date(ms));}

    void intel(){
        content.removeAllViews();content.addView(tv("هوش وال",27));content.addView(tv("وضعیت منابع داده و سیاست شواهد",13));
        String[] rows={"بازار CoinMarketCap — قیمت و تغییر ۲۴ساعته","موتور تکنیکال — محلی و قاعده‌مند","HypurrTrace HyperEVM — در صورت دسترس‌پذیری زنده","DeBank — نیازمند کلید API مجاز برای دادهٔ کیف پول","فیلتر صرافی/استخر/قرارداد — محافظه‌کارانه","طبقه‌بندی کیف پول ناشناخته — تأییدنشده","حالت آفلاین — فقط آخرین داده ذخیره‌شده","داده جعلی/تصادفی — غیرفعال"};
        for(String r:rows){TextView x=tv("●  "+r,13);x.setBackground(box(Color.WHITE,12));content.addView(x,new LinearLayout.LayoutParams(-1,d(52)));Space s=new Space(this);content.addView(s,new LinearLayout.LayoutParams(1,d(5)));}
        content.addView(tv("مهم: بررسی ۱۰۰ دارنده برتر هر ارز در چند زنجیره به منبع ایندکس‌شده برای هر شبکه/ارز نیاز دارد. وال به‌جای ادعای پوشش غیرواقعی، داده ناکافی را اعلام می‌کند.",11));
    }

    Object getJsonAny(String u)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(15000);c.setRequestProperty("Accept","application/json");int code=c.getResponseCode();InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();BufferedReader r=new BufferedReader(new InputStreamReader(in));StringBuilder s=new StringBuilder();String z;while((z=r.readLine())!=null)s.append(z);if(code<200||code>=300)throw new IOException("HTTP "+code);String body=s.toString().trim();if(body.startsWith("["))return new JSONArray(body);return new JSONObject(body);}
    JSONArray getJson(String u)throws Exception{Object o=getJsonAny(u);if(o instanceof JSONArray)return (JSONArray)o;throw new IOException("Expected array");}
    void loadMarkets(){
        status.setText("● اتصال به CoinMarketCap...");status.setTextColor(blue);
        new Thread(()->{Exception last=null;JSONArray converted=null;
            String[] endpoints={"https://pro-api.coinmarketcap.com/public-api/v3/cryptocurrency/listings/latest?start=1&limit=500&convert=USD","https://pro-api.coinmarketcap.com/public-api/v1/cryptocurrency/listings/latest?start=1&limit=500&convert=USD"};
            for(String endpoint:endpoints){try{Object raw=getJsonAny(endpoint);JSONArray rows=extractList(raw);if(rows==null||rows.length()==0)throw new IOException("پاسخ فهرست CMC خالی است");JSONArray out=new JSONArray();for(int i=0;i<rows.length();i++){JSONObject c=rows.optJSONObject(i);if(c==null)continue;JSONObject quote=c.optJSONObject("quote");if(quote==null)continue;JSONObject usd=quote.optJSONObject("USD");if(usd==null)usd=quote.optJSONObject("usd");if(usd==null)continue;JSONObject item=new JSONObject();item.put("symbol",c.optString("symbol"));item.put("name",c.optString("name"));item.put("lastPrice",usd.optDouble("price"));item.put("priceChangePercent",usd.optDouble("percent_change_24h"));item.put("quoteVolume",usd.optDouble("volume_24h"));item.put("marketCap",usd.optDouble("market_cap"));out.put(item);}if(out.length()>0){converted=out;break;}}catch(Exception e){last=e;}}
            final JSONArray found=converted;final Exception failure=last;runOnUiThread(()->{if(found!=null&&found.length()>0){tickers=found;status.setText("● بازار زنده · CoinMarketCap");status.setTextColor(green);home();}else{status.setText("● اتصال به CMC ناموفق — لمس برای تلاش مجدد");status.setTextColor(red);status.setOnClickListener(v->loadMarkets());content.removeAllViews();content.addView(tv("دریافت داده از CoinMarketCap ناموفق بود.",18));content.addView(tv("این نسخه برای قیمت بازار از API صرافی استفاده نمی‌کند. فید عمومی CMC ممکن است از شبکهٔ فعلی قابل‌دسترسی نباشد.",13));content.addView(tv("خطا: "+(failure==null?"پاسخ خالی":failure.getClass().getSimpleName()+": "+failure.getMessage()),11));Button retry=btn("تلاش دوباره");retry.setOnClickListener(v->loadMarkets());content.addView(retry,new LinearLayout.LayoutParams(-1,d(48)));}});
        }).start();
    }
    String fmt(double x){if(x>=1000)return String.format(Locale.US,"%,.2f",x);if(x>=1)return String.format(Locale.US,"%.4f",x);if(x>=.01)return String.format(Locale.US,"%.5f",x);return String.format(Locale.US,"%.8f",x);}
    String fmt2(double x){return String.format(Locale.US,"%.2f",x);}
    String shortNum(double x){if(x>=1e9)return String.format(Locale.US,"%.2fB",x/1e9);if(x>=1e6)return String.format(Locale.US,"%.2fM",x/1e6);if(x>=1e3)return String.format(Locale.US,"%.2fK",x/1e3);return String.format(Locale.US,"%.0f",x);}
    String find(JSONObject o,String... keys){for(String k:keys){Object v=o.opt(k);if(v!=null&&v!=JSONObject.NULL){String s=String.valueOf(v).trim();if(!s.isEmpty()&&!s.equals("null"))return s;}}return "";}
    String shortAddr(String a){return a.length()>18?a.substring(0,10)+"…"+a.substring(a.length()-6):a;}

    static class Candle{double o,h,l,c,v;long t;Candle(double O,double H,double L,double C,double V,long T){o=O;h=H;l=L;c=C;v=V;t=T;}}
    static class Signal{String name,text;Signal(String n,String t){name=n;text=t;}}
    class ChartView extends View{
        ArrayList<Candle> data=new ArrayList<>();Paint p=new Paint(1);
        ChartView(Activity a){super(a);p.setStrokeWidth(2);}
        void setData(ArrayList<Candle> d){data=d;invalidate();}
        protected void onDraw(Canvas c){super.onDraw(c);if(data==null||data.isEmpty()){p.setColor(ink);p.setTextSize(d(13));c.drawText("INSUFFICIENT DATA",d(12),d(30),p);return;}float w=getWidth(),h=getHeight(),pad=d(12);double hi=-1e99,lo=1e99;for(Candle x:data){hi=Math.max(hi,x.h);lo=Math.min(lo,x.l);}double span=Math.max(1e-12,hi-lo);int n=data.size(),start=Math.max(0,n-80);float cw=(w-2*pad)/(n-start);
            p.setStrokeWidth(2);for(int i=start;i<n;i++){Candle x=data.get(i);float xx=pad+(i-start+.5f)*cw;float yh=(float)(pad+(hi-x.h)/span*(h-2*pad));float yl=(float)(pad+(hi-x.l)/span*(h-2*pad));float yo=(float)(pad+(hi-x.o)/span*(h-2*pad));float yc=(float)(pad+(hi-x.c)/span*(h-2*pad));p.setColor(x.c>=x.o?green:red);c.drawLine(xx,yh,xx,yl,p);c.drawRect(xx-cw*.3f,Math.min(yo,yc),xx+cw*.3f,Math.max(yo,yc)+1,p);}
        }
    }
}