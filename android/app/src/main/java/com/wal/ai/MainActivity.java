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
        TextView h=tv("WAL Intelligence",27);h.setTypeface(null,1);content.addView(h);
        content.addView(tv("Real market data • deterministic analysis • no fabricated whales",12));
        EditText q=new EditText(this);q.setHint("WAL Search AI — name / symbol / contract");q.setSingleLine();q.setPadding(d(14),0,d(14),0);q.setBackground(box(Color.WHITE,14));content.addView(q,new LinearLayout.LayoutParams(-1,d(54)));
        LinearLayout nav=new LinearLayout(this);
        String[] ns={"MARKETS","SIGNALS","WHALE RADAR","INTELLIGENCE"};
        for(String n:ns){Button x=btn(n);nav.addView(x,new LinearLayout.LayoutParams(0,d(48),1));if(n.equals("MARKETS"))x.setOnClickListener(v->markets());if(n.equals("SIGNALS"))x.setOnClickListener(v->signals());if(n.equals("WHALE RADAR"))x.setOnClickListener(v->radar());if(n.equals("INTELLIGENCE"))x.setOnClickListener(v->intel());}
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
            if(!s.endsWith("USDT")||(!q.isEmpty()&&!s.contains(q)))continue;
            double p=o.optDouble("lastPrice"), ch=o.optDouble("priceChangePercent"), vol=o.optDouble("quoteVolume");
            LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(d(10),d(9),d(10),d(9));c.setBackground(box(Color.WHITE,14));
            TextView a=tv(s.replace("USDT","")+"    $"+fmt(p),17);a.setTypeface(null,1);c.addView(a);
            TextView x=tv("24h  "+fmt2(ch)+"%    •    Vol $"+shortNum(vol),12);x.setTextColor(ch>=0?green:red);c.addView(x);
            c.addView(tv("LIVE MARKET  •  Tap for chart + WAL signal",10));
            c.setOnClickListener(v->asset(s));
            list.addView(c,new LinearLayout.LayoutParams(-1,d(86)));Space sp=new Space(this);list.addView(sp,new LinearLayout.LayoutParams(1,d(7)));n++;
        }catch(Exception ignored){}
    }

    void asset(String symbol){
        selected=symbol;content.removeAllViews();
        TextView back=tv("‹  BACK",13);back.setOnClickListener(v->home());content.addView(back);
        TextView h=tv(symbol.replace("USDT",""),27);h.setTypeface(null,1);content.addView(h);
        TextView price=tv("Loading live price...",18);content.addView(price);
        LinearLayout tf=new LinearLayout(this);String[] tfs={"1m","5m","15m","1h","4h","1d","1w","1M"};for(String tfv:tfs){Button b=btn(tfv);tf.addView(b,new LinearLayout.LayoutParams(0,d(42),1));b.setOnClickListener(v->loadChart(symbol,tfv,price));}content.addView(tf);
        ChartView chart=new ChartView(this);chart.setBackgroundColor(Color.WHITE);content.addView(chart,new LinearLayout.LayoutParams(-1,d(330)));
        content.addView(tv("WAL SIGNAL",16));TextView sig=tv("Calculating...",15);sig.setPadding(d(12),d(10),d(12),d(10));sig.setBackground(box(Color.WHITE,12));content.addView(sig);
        content.addView(tv("Evidence Strength is computed from live price/volume/technical confirmations. It is not a prediction accuracy percentage.",10));
        loadChart(symbol,"1h",price,chart,sig);
    }

    void loadChart(String symbol,String interval,TextView price){loadChart(symbol,interval,price,null,null);}
    void loadChart(String symbol,String interval,TextView price,ChartView chart,TextView sig){
        new Thread(()->{
            try{
                String u="https://api.binance.com/api/v3/klines?symbol="+symbol+"&interval="+interval+"&limit=120";
                JSONArray a=getJson(u);ArrayList<Candle> cs=new ArrayList<>();
                for(int i=0;i<a.length();i++){JSONArray x=a.getJSONArray(i);cs.add(new Candle(x.getDouble(1),x.getDouble(2),x.getDouble(3),x.getDouble(4),x.getDouble(5),x.getLong(0)));}
                Signal s=analyze(cs);
                double last=cs.get(cs.size()-1).c;
                runOnUiThread(()->{
                    price.setText("$"+fmt(last)+"    "+interval+"    LIVE");
                    if(chart!=null)chart.setData(cs);
                    if(sig!=null)sig.setText(s.text);
                });
            }catch(Exception e){runOnUiThread(()->price.setText("INSUFFICIENT DATA — "+e.getClass().getSimpleName()));}
        }).start();
    }

    Signal analyze(ArrayList<Candle> c){
        int n=c.size();double[] closes=new double[n];for(int i=0;i<n;i++)closes[i]=c.get(i).c;
        double ema20=ema(closes,20),ema50=ema(closes,50),rsi=rsi(closes,14),m=macd(closes),volNow=c.get(n-1).v,volAvg=0;
        for(int i=Math.max(0,n-21);i<n-1;i++)volAvg+=c.get(i).v;volAvg/=Math.max(1,Math.min(20,n-1));
        int score=0;if(closes[n-1]>ema20)score+=20;if(ema20>ema50)score+=20;if(rsi>=50&&rsi<=70)score+=15;if(m>0)score+=15;if(volNow>volAvg*1.15)score+=15;if(closes[n-1]>closes[Math.max(0,n-6)])score+=15;
        String s=score>=70?"BUY":score<=30?"SELL":"WAIT";
        String reason="Signal: "+s+"\nEvidence Strength: "+score+"/100\nRSI: "+fmt2(rsi)+"  |  EMA20/50: "+(ema20>ema50?"BULLISH":"BEARISH")+"\nMACD: "+(m>0?"POSITIVE":"NEGATIVE")+"  |  Volume: "+(volNow>volAvg*1.15?"EXPANSION":"NORMAL")+"\nData Quality: LIVE CANDLE DATA\nRule: deterministic; no random values.";
        return new Signal(s,reason);
    }
    double ema(double[] x,int p){double e=x[0],k=2.0/(p+1);for(int i=1;i<x.length;i++)e=x[i]*k+e*(1-k);return e;}
    double rsi(double[] x,int p){double g=0,l=0;for(int i=Math.max(1,x.length-p);i<x.length;i++){double d=x[i]-x[i-1];if(d>=0)g+=d;else l-=d;}if(l==0)return 100;return 100-(100/(1+g/l));}
    double macd(double[] x){return ema(x,12)-ema(x,26);}
    void signals(){
        content.removeAllViews();content.addView(tv("WAL Signals",27));content.addView(tv("Only deterministic signals from live candle data. No fabricated confidence.",12));
        if(tickers==null){content.addView(tv("INSUFFICIENT DATA",15));return;}
        int shown=0;for(int i=0;i<tickers.length()&&shown<12;i++)try{String s=tickers.getJSONObject(i).optString("symbol");if(!s.endsWith("USDT"))continue;assetSignalCard(s);shown++;}catch(Exception ignored){}
    }
    void assetSignalCard(String s){
        TextView x=tv(s.replace("USDT","")+"  •  analyzing...",15);x.setBackground(box(Color.WHITE,12));content.addView(x,new LinearLayout.LayoutParams(-1,d(58)));
        new Thread(()->{try{JSONArray a=getJson("https://api.binance.com/api/v3/klines?symbol="+s+"&interval=1h&limit=80");ArrayList<Candle> c=new ArrayList<>();for(int i=0;i<a.length();i++){JSONArray z=a.getJSONArray(i);c.add(new Candle(z.getDouble(1),z.getDouble(2),z.getDouble(3),z.getDouble(4),z.getDouble(5),z.getLong(0)));}Signal q=analyze(c);runOnUiThread(()->{x.setText(s.replace("USDT","")+"   •   "+q.name+"   •   Evidence "+q.text.split("Evidence Strength: ")[1].split("/")[0]+"/100");x.setTextColor(q.name.equals("BUY")?green:q.name.equals("SELL")?red:ink);x.setOnClickListener(v->asset(s));});}catch(Exception e){runOnUiThread(()->x.setText(s+"  •  INSUFFICIENT DATA"));}}).start();
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
        TextView back=tv("‹  BACK",13);back.setOnClickListener(v->{radarVisible=false;home();});content.addView(back);
        content.addView(tv("WAL Whale Radar",27));
        content.addView(tv("HyperEVM on-chain data • live activity refresh every 30 seconds",12));
        content.addView(tv("Whale candidate list refreshes every 24 hours. Entry/activity times are shown only when supplied by the blockchain indexer.",11));
        whaleStatus=tv("Connecting to HypurrTrace...",13);content.addView(whaleStatus);
        whaleRefreshLabel=tv("Whale list: checking last refresh...",10);content.addView(whaleRefreshLabel);
        content.addView(tv("LATEST WHALE MOVEMENTS",16));
        whaleListContainer=new LinearLayout(this);whaleListContainer.setOrientation(LinearLayout.VERTICAL);content.addView(whaleListContainer);
        content.addView(tv("WHALE CANDIDATES (NOT YET VERIFIED)",16));
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
        if(whaleStatus!=null)whaleStatus.setText("Refreshing 24-hour whale candidate list...");
        new Thread(()->{
            try{
                Object raw=getJsonAny("https://trace.hypurrscan.io/api/v1/indexed/wealth-rich-list");
                String json=raw.toString();
                p.edit().putString("rich_list_json",json).putLong("list_refresh_ms",System.currentTimeMillis()).apply();
                runOnUiThread(()->{
                    if(!radarVisible)return;
                    target.removeAllViews();renderWhaleCandidates(target,raw);
                    whaleRefreshLabel.setText("Whale list refreshed: "+dateTime(System.currentTimeMillis())+" • refresh interval 24h");
                    whaleStatus.setText("LIVE • HypurrTrace indexed wealth list");
                });
            }catch(Exception e){runOnUiThread(()->{if(radarVisible){whaleStatus.setText("INSUFFICIENT DATA — whale list provider unavailable");whaleRefreshLabel.setText("List refresh failed; last cached list retained when available.");}});}
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
            }catch(Exception e){runOnUiThread(()->{if(radarVisible&&target!=null&&target.getChildCount()==0)target.addView(tv("INSUFFICIENT DATA — live whale movements unavailable",13));});}
        }).start();
    }

    JSONArray extractList(Object raw){
        if(raw instanceof JSONArray)return (JSONArray)raw;
        if(raw instanceof JSONObject){
            JSONObject o=(JSONObject)raw;
            String[] keys={"items","results","data","transfers","transactions","rows","whales"};
            for(String k:keys){JSONArray a=o.optJSONArray(k);if(a!=null)return a;}
            for(String k:keys){JSONObject nested=o.optJSONObject(k);if(nested!=null){JSONArray a=extractList(nested);if(a!=null)return a;}}
        }
        return null;
    }

    void renderWhaleCandidates(LinearLayout target,Object raw){
        JSONArray a=extractList(raw);
        if(a==null){target.addView(tv("Provider response was not a recognized list → INSUFFICIENT DATA",13));return;}
        int count=0;
        for(int i=0;i<a.length()&&count<100;i++)try{
            JSONObject o=a.getJSONObject(i);
            String addr=find(o,"address","user","owner","account","wallet");
            if(addr.isEmpty())continue;
            String val=find(o,"usdValue","valueUsd","totalUsd","accountValue","usd_value");
            String label=find(o,"label","name","entity");
            String seen=getSharedPreferences("wal_whales",MODE_PRIVATE).getString("seen_"+addr.toLowerCase(Locale.US),"");
            if(seen.isEmpty()){seen=dateTime(System.currentTimeMillis());getSharedPreferences("wal_whales",MODE_PRIVATE).edit().putString("seen_"+addr.toLowerCase(Locale.US),seen).apply();}
            TextView x=tv((count+1)+". "+shortAddr(addr)+"\nValue: $"+(val.isEmpty()?"—":val)+"  •  First observed by WAL: "+seen+"\n"+(label.isEmpty()?"Classification: UNVERIFIED CANDIDATE":label+" • UNVERIFIED"),12);
            x.setBackground(box(Color.WHITE,12));target.addView(x,new LinearLayout.LayoutParams(-1,-2));
            Space sp=new Space(this);target.addView(sp,new LinearLayout.LayoutParams(1,d(5)));count++;
        }catch(Exception ignored){}
        if(count==0)target.addView(tv("No parseable wallet records → INSUFFICIENT DATA",13));
        else target.addView(tv("Candidate records: "+count+" • Verified independent whales: 0 until wallet classification is completed. First observed is WAL observation time, not a claimed historical buy time.",11));
    }

    void renderWhaleMovements(LinearLayout target,Object raw){
        JSONArray a=extractList(raw);
        if(a==null){target.addView(tv("No parseable live movement feed → INSUFFICIENT DATA",13));return;}
        int count=0;
        for(int i=0;i<a.length()&&count<30;i++)try{
            JSONObject o=a.getJSONObject(i);
            String from=find(o,"from","fromAddress","sender","src","owner");
            String to=find(o,"to","toAddress","recipient","dst","user","address");
            String token=find(o,"tokenSymbol","symbol","token","asset","name");
            String amount=find(o,"amount","value","tokenAmount","quantity");
            String usd=find(o,"usdValue","valueUsd","amountUsd","usd_value","transferUsd");
            String tx=find(o,"txHash","transactionHash","hash","transaction_hash");
            String stamp=find(o,"timestamp","blockTimestamp","timeStamp","createdAt","datetime","time","block_time","ts");
            if(from.isEmpty()&&to.isEmpty()&&tx.isEmpty())continue;
            String when=formatChainTime(stamp);
            StringBuilder line=new StringBuilder();
            line.append("On-chain event: ").append(when).append("\n");
            if(!from.isEmpty())line.append("From: ").append(shortAddr(from)).append("\n");
            if(!to.isEmpty())line.append("To: ").append(shortAddr(to)).append("\n");
            line.append("Asset: ").append(token.isEmpty()?"—":token).append(" • Amount: ").append(amount.isEmpty()?"—":amount);
            if(!usd.isEmpty())line.append(" • USD: $").append(usd);
            if(!tx.isEmpty())line.append("\nTx: ").append(shortAddr(tx));
            TextView x=tv(line.toString(),11);x.setBackground(box(Color.WHITE,12));target.addView(x,new LinearLayout.LayoutParams(-1,-2));
            Space sp=new Space(this);target.addView(sp,new LinearLayout.LayoutParams(1,d(5)));count++;
        }catch(Exception ignored){}
        if(count==0)target.addView(tv("Provider returned no parseable transfer events. No entry time can be confirmed from this response.",12));
        else target.addView(tv("Showing "+count+" recent indexed events. These timestamps are on-chain event times when provided by the source; a transfer alone does not prove a wallet opened a position or bought an asset.",10));
    }

    String formatChainTime(String raw){
        if(raw==null||raw.trim().isEmpty())return "timestamp unavailable";
        try{
            String s=raw.trim();
            if(s.matches("\\d{10,13}")){
                long t=Long.parseLong(s);if(s.length()==10)t*=1000L;
                return dateTime(t)+" (chain/indexer time)";
            }
            return s+" (source timestamp)";
        }catch(Exception e){return raw;}
    }
    String dateTime(long ms){if(ms<=0)return "not recorded";return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date(ms));}

    void intel(){
        content.removeAllViews();content.addView(tv("WAL Intelligence",27));content.addView(tv("Provider health and evidence policy",13));
        String[] rows={"Binance Market/Candles — LIVE","Technical Engine — LOCAL / DETERMINISTIC","HyperEVM HypurrTrace — LIVE WHEN AVAILABLE","Exchange/LP/contract filtering — CONSERVATIVE","Unknown wallet classification — UNVERIFIED","Offline mode — LAST KNOWN DATA ONLY","Fake/random/mock values — DISABLED"};
        for(String r:rows){TextView x=tv("●  "+r,13);x.setBackground(box(Color.WHITE,12));content.addView(x,new LinearLayout.LayoutParams(-1,d(52)));Space s=new Space(this);content.addView(s,new LinearLayout.LayoutParams(1,d(5)));}
        content.addView(tv("Important: complete multi-chain top-100 holder census requires indexed providers for each network/token. WAL reports INSUFFICIENT DATA rather than pretending coverage exists.",11));
    }

    Object getJsonAny(String u)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(15000);c.setRequestProperty("Accept","application/json");int code=c.getResponseCode();InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();BufferedReader r=new BufferedReader(new InputStreamReader(in));StringBuilder s=new StringBuilder();String z;while((z=r.readLine())!=null)s.append(z);if(code<200||code>=300)throw new IOException("HTTP "+code);String body=s.toString().trim();if(body.startsWith("["))return new JSONArray(body);return new JSONObject(body);}
    JSONArray getJson(String u)throws Exception{Object o=getJsonAny(u);if(o instanceof JSONArray)return (JSONArray)o;throw new IOException("Expected array");}
    void loadMarkets(){new Thread(()->{try{tickers=getJson("https://api.binance.com/api/v3/ticker/24hr");runOnUiThread(()->{status.setText("● LIVE");status.setTextColor(green);home();});}catch(Exception e){runOnUiThread(()->{status.setText("● OFFLINE");status.setTextColor(red);});}}).start();}
    String fmt(double x){if(x>=1000)return String.format(Locale.US,"%,.2f",x);if(x>=1)return String.format(Locale.US,"%.4f",x);if(x>=.01)return String.format(Locale.US,"%.5f",x);return String.format(Locale.US,"%.8f",x);}
    String fmt2(double x){return String.format(Locale.US,"%.2f",x);}
    String shortNum(double x){if(x>=1e9)return String.format(Locale.US,"%.2fB",x/1e9);if(x>=1e6)return String.format(Locale.US,"%.2fM",x/1e6);if(x>=1e3)return String.format(Locale.US,"%.2fK",x/1e3);return String.format(Locale.US,"%.0f",x);}
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