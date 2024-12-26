package com.example.gestordecumples.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.util.Calendar;
import android.net.Uri;
import android.provider.ContactsContract;

import com.example.gestordecumples.models.Contact;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DBHelper extends SQLiteOpenHelper {


    private static final String DB_NAME = "CUMPLEANIOS";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "miscumples";
    private static Optional<ArrayList<Contact>>optListContactos = Optional.empty();
    private final Context isTheFinalContext;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.isTheFinalContext = context;
    }

    public Optional<ArrayList<Contact>> getOptListContactos() {
        return optListContactos;
    }
    public Optional<ArrayList<Contact>> getAndGenerateOptListContactos() {

        obtenerDatosContactos(isTheFinalContext.getContentResolver());
        sincronizarOptionalDesdeBD();

        return optListContactos;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String queryCrearTabla = "CREATE TABLE IF NOT EXISTS miscumples(ID integer,TipoNotif char(1),Mensaje VARCHAR(160),Telefono VARCHAR(15),FechaNacimiento VARCHAR(15),Nombre VARCHAR(128));";
        db.execSQL(queryCrearTabla);
        obtenerDatosContactos(isTheFinalContext.getContentResolver());
        rellenarBD(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void sincronizarOptionalDesdeBD(){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("Select * from miscumples",null,null);

        if(c.getCount() > 0){

            while (c.moveToNext()) {

                int numId = c.getColumnIndex("ID");
                String id = c.getString(numId);
                int numTipoNotif = c.getColumnIndex("TipoNotif");
                String tipoNotif = c.getString(numTipoNotif);
                int numMensaje = c.getColumnIndex("Mensaje");
                String mensaje = c.getString(numMensaje);
                int numTelefono = c.getColumnIndex("Telefono");
                String telefono = c.getString(numTelefono);
                int numFechaNacimiento = c.getColumnIndex("FechaNacimiento");
                String fechaNacimiento = c.getString(numFechaNacimiento);

                if (getOptListContactos().isPresent() && !getOptListContactos().get().isEmpty()){

                    Contact contacto = getOptListContactos().get()
                            .stream()
                            .filter( con -> con.getId() == Integer.parseInt(id))
                            .findFirst()
                            .orElse(null);

                    if (contacto != null){
                        contacto.setTipoNotif(tipoNotif);
                        contacto.setMensaje(mensaje);
                        contacto.setTelefono(telefono);
                        contacto.setFechaNacimiento(fechaNacimiento);
                    }
                }
            }
        }
        c.close();
        db.close();
    }

    public void obtenerDatosContactos(ContentResolver contentResolver){

        String[] proyeccion ={ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_ID};
        ArrayList<Contact>lista = new ArrayList<>();
        // ArrayList<String>listaTelefonos = new ArrayList<>();
        Cursor cur = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, proyeccion, null, null, null);

        if (cur.getCount() > 0) {

            while (cur.moveToNext()) {
                //Con este formato recibo un error de que el valor debe ser mayor o igual que 0
                //String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                int numId = cur.getColumnIndex(ContactsContract.Contacts._ID);
                String id = cur.getString(numId);
                int numName = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String name = cur.getString(numName);
                int numHasPhoneNumber = cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                String hasPhoneNumber = cur.getString(numHasPhoneNumber);
                int numPhotoId = cur.getColumnIndex(ContactsContract.Contacts.PHOTO_ID);
                String photoId = cur.getString(numPhotoId);
                URI rutaImg = null;//android.net.Uri not Serializable - java.net.URI SI serializable

                if (photoId != null) {
                    try {
                        rutaImg = new URI(Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, photoId).toString());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }

                ArrayList<String> listaTelefonos = new ArrayList<String>();

                if (Integer.parseInt(hasPhoneNumber) > 0) {
                    listaTelefonos = obtenerNumTelefono(id);
                    lista.add(new Contact(Integer.parseInt(id),"0", listaTelefonos.get(0), name, photoId, rutaImg, listaTelefonos));
                }
                else{

                    lista.add(new Contact(Integer.parseInt(id),"0", "", name, photoId, rutaImg, listaTelefonos));
                }

            }

            optListContactos = Optional.of(lista);
        }
        cur.close();
    }


    private ArrayList<String> obtenerNumTelefono(String id){

        ArrayList<String> listTelefono = new ArrayList<>();
        ContentResolver cr = isTheFinalContext.getContentResolver();
        Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                new String[]{id}, null);

        while (c.moveToNext()) {

            int numColum = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
            listTelefono.add(c.getString(numColum));
        }
        c.close();
        return listTelefono;
    }


    public void rellenarBD(SQLiteDatabase db) {

        if (optListContactos.isPresent() && !optListContactos.get().isEmpty()){
            //En vez de generar mi propio SQLiteDatabase, aprovecho uno ya creado para evitar llamadas redundantes
            //SQLiteDatabase db = this.getWritableDatabase();

            optListContactos
                    .get()
                    .forEach( o ->{
                        ContentValues values = new ContentValues();
                        values.put("ID", o.getId());
                        values.put("TipoNotif", o.getTipoNotif());//char
                        values.put("Telefono", o.getTelefono());
                        values.put("Nombre", o.getNombre());
                        db.insert(TABLE_NAME, null, values);
                    });
            //db.close();
        }
    }

    public boolean guardarContactoDesdeVerContacto(Optional<HashMap<String,String>>optMapDatos){

        boolean result = false;

        if (optMapDatos.isPresent()){
            result = guardarContactoBD(optMapDatos.get());
            guardarContactoOptional(optMapDatos.get());
        }

        return result;
    }

    private boolean guardarContactoBD(HashMap<String,String>mapDatos){

        boolean result = false;
        int count;

        SQLiteDatabase db = this.getWritableDatabase();

        String [] args_filtro = {mapDatos.get("ID")};
        ContentValues values = new ContentValues();
        values.put("TipoNotif", mapDatos.get("TipoNotif"));//char
        values.put("Telefono", mapDatos.get("Telefono"));
        values.put("FechaNacimiento", mapDatos.get("FechaNacimiento"));
        values.put("Mensaje", mapDatos.get("Mensaje"));

        count = db.update(TABLE_NAME,values,"ID = ?",args_filtro);
        db.close();

        if (count > 0){
            result = true;
        }

        return result;
    }

    private void guardarContactoOptional(HashMap<String,String>mapDatos){

        if (optListContactos.isPresent() && !optListContactos.get().isEmpty()){

            optListContactos
                    .get()
                    .stream()
                    .filter( c -> c.getId() == Integer.parseInt(Objects.requireNonNull(mapDatos.get("ID"))))
                    .forEach( contac -> {
                                contac.setTipoNotif(mapDatos.get("TipoNotif"));
                                contac.setTelefono(mapDatos.get("Telefono"));
                                contac.setFechaNacimiento(mapDatos.get("FechaNacimiento"));
                                contac.setMensaje(mapDatos.get("Mensaje"));
                            }
                    );
        }
    }

    public Optional<ArrayList<Contact>> quienCumpleHoy(){

        if ( !getOptListContactos().isPresent()){
            getAndGenerateOptListContactos();
        }

        final Calendar c = Calendar.getInstance();
        int mes = (c.get(Calendar.MONTH))+ 1; //El mes empieza por 0
        int dia = (c.get(Calendar.DAY_OF_MONTH)) ;

        ArrayList<Contact> listContacFiltrada = getOptListContactos().get()
                .stream()
                .filter( a -> a.getFechaNacimiento() != null) //Fechas vacias
                .filter( co -> {
                    String[] fecha = Arrays.stream(co.getFechaNacimiento().split("/")).map(String::trim).toArray(String[]::new);
                    return (fecha[0].equals(String.valueOf(dia))) && (fecha[1].equals(String.valueOf(mes)));
                })
                .collect(Collectors.toCollection(ArrayList::new));

        return Optional.of(listContacFiltrada);
    }
}