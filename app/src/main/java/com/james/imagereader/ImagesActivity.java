package com.james.imagereader;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.james.imageassets.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * TODO: 沉浸式
 *  1.子线程加载assets
 *  2.多加几种图片格式
 *  #3.把 progress 和 offset 保存到数据库
 *  4.如何判断图片是否完整？
 *  5.滚动手法能否做的更好一些
 *  6.优化滑动速度
 *  7.左滑右滑上一章/下一章
 */
public class ImagesActivity extends BaseActivity {
    private final ArrayList<String> imageList = new ArrayList<>();
    private FrameLayout parentView;
    private RecyclerView rv_image;

    private int screenWidth;
    private LinearLayoutManager layoutManager;
    private AssetManager pluginAsset;
    private Resources pluginResources;
    private String packageName;
    private RelativeLayout rl_toolbar;
    private TextView tv_progress;
    private ImageView iv_cover;
    private CheckBox cb_fav;
    private int imageCount;
    private int albumIndex;
    private int progress;
    private int offset;
    private long packageSize;
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    private final static String BANNER_ID = "847565980675487_847664703998948";
    private final static String INTERS_ID = "847565980675487_851457520286333";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        parentView = findViewById(R.id.parent);
        rv_image = findViewById(R.id.rv_image);
        iv_cover = findViewById(R.id.iv_cover);

        iv_cover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                iv_cover.setVisibility(View.GONE);
                rv_image.setVisibility(View.VISIBLE);
                return true;
            }
        });
        pluginAsset = getAssets();
        // progress
        tv_progress = findViewById(R.id.tv_progress);
        try {
            String[] imageFiles = pluginAsset.list("imgs");
            assert imageFiles != null;
            for (String imageFile : imageFiles) {
                if (imageFile.endsWith(".jpg") || imageFile.endsWith(".webp")) {
                    imageList.add(imageFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageCount = imageList.size();
        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_image.setLayoutManager(layoutManager);
        rv_image.setAdapter(new ImageAdapter());
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        layoutManager.scrollToPositionWithOffset(progress, offset);
        rv_image.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    mHandler.removeMessages(0);
                    countTimer = 0;
                }
                updateProgress();
            }
        });
        rv_image.post(new Runnable() {
            @Override
            public void run() {
                updateProgress();
            }
        });
        layoutManager.scrollToPositionWithOffset(loadData("position"), loadData("offset"));
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                getResponse();
//            }
//        }).start();
    }

    private void getResponse() {
        try {
            URL url = new URL("http://baidu.com/");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(6 * 1000);
            InputStream inputStream = httpURLConnection.getInputStream();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            char[] buff = new char[1000];
            while (inputStreamReader.read(buff) != -1) {
                String response = new String(buff);
            }
        } catch (IOException e) {
        }
    }

    private void updateProgress() {
        progress = layoutManager.findLastVisibleItemPosition();
        View lastView = layoutManager.findViewByPosition(progress);
        if (lastView != null) {
            offset = lastView.getTop();
        }
        int mProgress = progress + 1;
        String percent = decimalFormat.format((mProgress * 100 / (float) imageCount));
        tv_progress.setText(String.valueOf("P" + mProgress + "/" + imageCount + " " + percent + "%"));
        saveData("position", progress);
        saveData("offset", offset);
    }

    private int countTimer = 0;
    private boolean isRunning = true;

    private void startScroll() {
        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    SystemClock.sleep(8);
                    if (countTimer > 250) {
                        mHandler.sendEmptyMessage(0);
                    }
                    countTimer++;
                }
            }
        }).start();
    }

    private void stopScroll() {
        isRunning = false;
        countTimer = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startScroll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScroll();
        rv_image.setVisibility(View.INVISIBLE);
        iv_cover.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        rv_image.setVisibility(View.INVISIBLE);
        iv_cover.setVisibility(View.VISIBLE);
        super.onStop();
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            rv_image.scrollBy(scrollSpeed, scrollSpeed);
        }
    };
    private int scrollSpeed = 0;

    class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ImageViewHolder(View.inflate(mContext, R.layout.item_list_image, null));
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            InputStream imageStream = null;
            Rect rect = new Rect(0, 0, 100, 100);
            options.inSampleSize = 1;
            try {
                imageStream = pluginAsset.open("imgs/" + imageList.get(position));
                Bitmap mBitmap = BitmapFactory.decodeStream(imageStream, rect, options);
                int bWidth = options.outWidth;
                int bHeight = options.outHeight;
                if (bWidth == -1 || bHeight == -1) {
                    throw new IOException();
                }
                int iWidth = screenWidth;
                int iHeight = iWidth * bHeight / bWidth;
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(iWidth, iHeight);
                holder.iv_photo.setLayoutParams(layoutParams);
                holder.iv_photo.setImageBitmap(mBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                holder.iv_photo.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
            } finally {
                if (imageStream != null) {
                    try {
                        imageStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return imageCount;
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_photo;
        TextView tv_name;

        public ImageViewHolder(View itemView) {
            super(itemView);
            iv_photo = itemView.findViewById(R.id.iv_photo);
            iv_photo.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    /*
                     * TODO:
                     *  #1.卸载
                     *  2.保存
                     *  3.goto
                     *  4.分享
                     */
                    //uninstall(packageName);
                    return true;
                }
            });
            iv_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scrollSpeed = (scrollSpeed + 5) % 15;
                }
            });
            tv_name = itemView.findViewById(R.id.tv_name);
        }
    }
}
