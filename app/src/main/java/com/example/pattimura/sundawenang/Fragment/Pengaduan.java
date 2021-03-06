package com.example.pattimura.sundawenang.Fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pattimura.sundawenang.R;
import com.example.pattimura.sundawenang.Utility;
import com.example.pattimura.sundawenang.VolleyHelper.AppHelper;
import com.example.pattimura.sundawenang.VolleyHelper.VolleyMultipartRequest;
import com.example.pattimura.sundawenang.VolleyHelper.VolleySingleton;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.drakeet.materialdialog.MaterialDialog;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class Pengaduan extends Fragment implements View.OnClickListener {
    private Uri pajakFileUri = null;
    private Uri ktpFileUri = null;
    private File filesktp, filespajak;
    private String status = "", userChoosenTask = "";
    private ImageView ktp, pajak;
    private String namafile;
    private Button submit;
    private TextView txtKtp, txtPajak;
    private MaterialEditText isiPengaduan;
    private Drawable picktp, picpajak;
    private MaterialDialog mMaterialDialog;
    public int total = 0;
    private boolean result = false;

    public Pengaduan() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pengaduan, container, false);

        isiPengaduan = (MaterialEditText) v.findViewById(R.id.txtisiPengaduan);
        txtKtp = (TextView) v.findViewById(R.id.txtKTPpengaduan);
        txtPajak = (TextView) v.findViewById(R.id.txtPajakpengaduan);
        ktp = (ImageView) v.findViewById(R.id.imageViewKTPPengaduan);
        pajak = (ImageView) v.findViewById(R.id.imageViewPajakPengaduan);
        submit = (Button) v.findViewById(R.id.buttonPengaduan);


        Picasso.with(this.getContext()).load(R.drawable.buttonuploadfotobiru).fit().into(ktp);
        Picasso.with(this.getContext()).load(R.drawable.buttonuploadfotoijo).fit().into(pajak);

        ktp.setOnClickListener(this);
        pajak.setOnClickListener(this);
        submit.setOnClickListener(this);

        getTotalData();

        return v;
    }

    @Override
    public void onClick(View v) {
        if (v == ktp) {
            status = "ktp";
            photoBuilder();
        } else if (v == pajak) {
            status = "pajak";
            photoBuilder();
        } else if (v == submit) {
            if (!isiPengaduan.getText().toString().equals("") && ktpFileUri != null && pajakFileUri != null) {
                kirimData(isiPengaduan.getText().toString(), ktpFileUri, pajakFileUri);
                View vi = View.inflate(getContext(), R.layout.layoutdialog, null);
                mMaterialDialog = new MaterialDialog(Pengaduan.this.getContext())
                        .setTitle("Tenant Changed !")
                        .setView(vi)
                        //.setMessage("Tenant name : " + getArguments().getString("nama") + "\nDescription : " + getArguments().getString("desc") + "\nStatus : " + getArguments().getString("status"))
                        .setPositiveButton("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMaterialDialog.dismiss();
                            }
                        });
                mMaterialDialog.show();
                clearData();
            } else {
                Toast.makeText(Pengaduan.this.getContext(), "Tolong lengkapi, isi pengaduan atau kelengkapan foto anda !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void clearData() {
        isiPengaduan.setText("");
        ktpFileUri = null;
        pajakFileUri = null;
        Picasso.with(this.getContext()).load(R.drawable.buttonuploadfotobiru).fit().into(ktp);
        Picasso.with(this.getContext()).load(R.drawable.buttonuploadfotoijo).fit().into(pajak);
        txtKtp.setText("Upload Foto KTP");
        txtPajak.setText("Upload Foto Pajak");
    }

    private void getTotalData() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://212.237.31.161/api/report", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject listdata = new JSONObject(response);
                    total = listdata.getInt("total");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(Pengaduan.this.getContext(), "Tidak dapat memuat data\nTolong perika koneksi internet anda !", Toast.LENGTH_LONG).show();
                    //hideProgressDialog();
                }
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this.getContext());
        requestQueue.add(stringRequest);
    }

    private void kirimData(final String isi, final Uri ktpuri, final Uri pajakuri) {
        //final ProgressDialog dialog = ProgressDialog.show(TambahAspirasi.this.getContext(), "", "Loading. Please wait...", true);
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, "http://212.237.31.161/api/report", new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
//                try {

//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(Pengaduan.this.getContext(), "Tidak dapat memuat data\nTolong perika koneksi internet anda !", Toast.LENGTH_LONG).show();
                    //hideProgressDialog();
                }
                //dialog.dismiss();

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", "Report " + Integer.toString(total + 1));
                params.put("report", isi);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("photo_id", new DataPart(ktpuri.getLastPathSegment(), AppHelper.getFileDataFromDrawable(Pengaduan.this.getContext(), picktp), "image/jpeg"));
                params.put("photo_tax", new DataPart(pajakuri.getLastPathSegment(), AppHelper.getFileDataFromDrawable(Pengaduan.this.getContext(), picpajak), "image/jpeg"));
                return params;
            }

        };

        VolleySingleton.getInstance(Pengaduan.this.getContext()).addToRequestQueue(multipartRequest);
    }


    private void photoBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Pengaduan.this.getContext());
        builder.setPositiveButton("Ambil Gambar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userChoosenTask = "Ambil Foto";
                result = Utility.checkPermission(Pengaduan.this.getContext());
                if (result) {
                    launchCamera();
                }
            }
        });
        builder.setNegativeButton("Pilih dari Galery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userChoosenTask = "Pilih dari Galeri";
                result = Utility.checkPermission(Pengaduan.this.getContext());
                if (result) {
                    pickdariGalery();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    //code for deny
                }
                break;
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickdariGalery();
                } else {
                    //code for deny
                }
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    Pengaduan.this.getActivity().finish();
                    return true;
                }
                return false;
            }
        });
    }

    private void launchCamera() {

        // Create intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Choose file storage location
        File file = new File(Environment.getExternalStorageDirectory(), UUID.randomUUID().toString() + ".jpg");
        if (status.equals("ktp")) {
            filesktp = file;
            ktpFileUri = FileProvider.getUriForFile(Pengaduan.this.getContext(), Pengaduan.this.getActivity().getApplicationContext().getPackageName() + ".provider", file);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, ktpFileUri);
            namafile = ktpFileUri.getLastPathSegment();
        } else if (status.equals("pajak")) {
            filespajak = file;
            pajakFileUri = FileProvider.getUriForFile(Pengaduan.this.getContext(), Pengaduan.this.getActivity().getApplicationContext().getPackageName() + ".provider", file);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pajakFileUri);
            namafile = pajakFileUri.getLastPathSegment();
        }
        // Launch intent
        startActivityForResult(takePictureIntent, 0);
    }

    private void pickdariGalery() {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                if (status.equals("ktp")) {
                    txtKtp.setText(namafile);
                    picktp = Drawable.createFromPath(filesktp.getPath());
                    Picasso.with(Pengaduan.this.getContext()).load(filesktp).fit().into(ktp);
                } else if (status.equals("pajak")) {
                    txtPajak.setText(namafile);
                    picpajak = Drawable.createFromPath(filespajak.getPath());
                    Picasso.with(Pengaduan.this.getContext()).load(filespajak).fit().into(pajak);
                }
            } else {
                Toast.makeText(Pengaduan.this.getContext(), "Photo gagal diambil, tolong ulangi lagi !", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = Pengaduan.this.getContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                File file = new File(filePath);
                if (status.equals("ktp")) {
                    filesktp = file;
                    ktpFileUri = Uri.fromFile(file);
                    picktp = Drawable.createFromPath(filesktp.getPath());
                    txtKtp.setText(ktpFileUri.getLastPathSegment());
                    Picasso.with(Pengaduan.this.getContext()).load(filesktp).fit().into(ktp);
                } else if (status.equals("pajak")) {
                    filespajak = file;
                    pajakFileUri = Uri.fromFile(file);
                    picpajak = Drawable.createFromPath(filespajak.getPath());
                    txtPajak.setText(pajakFileUri.getLastPathSegment());
                    Picasso.with(Pengaduan.this.getContext()).load(filespajak).fit().into(pajak);
                }
            } else {
                Toast.makeText(Pengaduan.this.getContext(), "Photo gagal diambil, tolong ulangi lagi !", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
