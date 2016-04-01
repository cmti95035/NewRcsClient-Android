package mobisocial.musubi.cloudstorage;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import mobisocial.musubi.R;

/**
 * The adapter to hold all the backup files from a single cloud storage.
 */
public class FileAdapter extends BaseAdapter {

    private static final String TAG = "FileAdapter";
    private static List<FileDescription> mData;

    private LayoutInflater mInflater;
    private Context mContext;
    private long[] mLens;

    private final class ViewHolder {
        public ImageView fileIcon;
        public TextView fileName;
        public TextView lastModified;
    }

    public FileAdapter(Context context, String[] files, long[] lens) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mLens = lens;
        mData = extractData(files);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;

        // cloud_storage_lose is the cross to remove the storage link, it
        // can be used to show the last back-up stamp -- Charlie
        if (convertView == null) {

            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.file_item, null);
            holder.fileIcon = (ImageView) convertView.findViewById
                    (R.id.file_icon);
            holder.fileName = (TextView) convertView.findViewById(R
                    .id.file_name);
            holder.lastModified = (TextView) convertView.findViewById
                    (R.id.last_modified);
            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        holder.fileIcon.setBackgroundResource(R.drawable.file);
        holder.fileName.setText(mData.get(position)
                .getDisplayName());

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                mContext.getResources()
                .getConfiguration().locale);
        String datStr = sf.format(mData.get(position)
                .getLastModified());
        holder.lastModified.setText(datStr);

        return convertView;
    }

    private List<FileDescription> extractData(String[] files) {

        List<FileDescription> data = new ArrayList<FileDescription>();
        int i = 0;
        if ( null!= files) {
            for (String str : files) {
                int index = str.lastIndexOf(".");
                int start = str.lastIndexOf("/");
                String dt = str.substring(index + 1, str.length());
                // server does not use real time stamp long
                Date d;
                try {
                    //d = new Date(Long.valueOf(dt));
                    d = new Date(Integer.parseInt(dt.substring(0,4))-1900,
                            Integer.parseInt(dt.substring(4,6))-1,
                            Integer.parseInt(dt.substring(6,8)),
                            Integer.parseInt(dt.substring(8,10)),
                            Integer.parseInt(dt.substring(10,12)),
                            Integer.parseInt(dt.substring(12,14)));
                } catch (Exception e) {
                    Log.e(TAG, "File name parsing error", e);
                    continue;
                }
                data.add(new FileDescription(str, str.substring(start+1, index),
                        d, mLens[i++]));

            }
        }
        Collections.sort(data, new Comparator<FileDescription>() {
            @Override
            public int compare(FileDescription lhs, FileDescription rhs) {
                return -lhs.getLastModified().compareTo(rhs.getLastModified());
            }
        });
        return data;
    }
}
