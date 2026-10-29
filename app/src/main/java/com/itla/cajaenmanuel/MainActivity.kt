package com.itla.cajaenmanuel

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.text.Html
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MainActivity : Activity() {
    private lateinit var body: LinearLayout
    private var generation = 0
    private var isHome = true
    private var player: MediaPlayer? = null
    private val ink = Color.rgb(23, 38, 57)
    private val blue = Color.rgb(30, 99, 180)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); home() }
    override fun onDestroy() { player?.release(); super.onDestroy() }
    override fun onBackPressed() { if (isHome) finish() else home() }
    private fun shape(color: Int, radius: Int = 14): GradientDrawable = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun txt(s: String, size: Float = 16f, bold: Boolean = false): TextView = TextView(this).apply { text = s; textSize = size; setTextColor(ink); if (bold) setTypeface(null, Typeface.BOLD); setPadding(0,dp(5),0,dp(5)) }
    private fun label(s: String, size: Float = 16f, bold: Boolean = false) { body.addView(txt(s,size,bold)) }
    private fun button(s: String, run: () -> Unit) { body.addView(Button(this).apply { text=s; isAllCaps=false; setTextColor(Color.WHITE); background=shape(blue); setOnClickListener { run() } }, LinearLayout.LayoutParams(-1,dp(52)).apply { setMargins(0,dp(7),0,dp(7)) }) }
    private fun input(hint: String): EditText = EditText(this).apply { this.hint=hint; setSingleLine(true); setPadding(dp(12),0,dp(12),0); background=shape(Color.WHITE,10); body.addView(this,LinearLayout.LayoutParams(-1,dp(52))) }
    private fun setup(title: String, back: Boolean = true) {
        generation++; isHome = !back; player?.release(); player=null
        val scroll=ScrollView(this); body=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(20),dp(24),dp(20),dp(30)); setBackgroundColor(Color.rgb(242,246,251)) }; scroll.addView(body); setContentView(scroll)
        if(back) button("← Volver al inicio") { home() }
        label(title,26f,true)
    }
    private fun imageResource(id:Int, height:Int=190) { body.addView(ImageView(this).apply { setImageResource(id); scaleType=ImageView.ScaleType.FIT_CENTER; contentDescription="Imagen" },LinearLayout.LayoutParams(-1,dp(height))) }
    private fun remoteImage(url:String, height:Int=170) {
        val image=ImageView(this).apply { scaleType=ImageView.ScaleType.FIT_CENTER; contentDescription="Foto" }; body.addView(image,LinearLayout.LayoutParams(-1,dp(height)))
        val token=generation
        Thread { try { val c=URL(url).openConnection() as HttpURLConnection; c.connectTimeout=12000; c.readTimeout=12000; val b=c.inputStream.use { BitmapFactory.decodeStream(it) }; c.disconnect(); runOnUiThread { if(token==generation) image.setImageBitmap(b) } } catch (_:Exception) {} }.start()
    }
    private fun get(url:String, target:LinearLayout, render:(String)->Unit) {
        val token=generation; target.removeAllViews(); target.background=null; target.setPadding(0,0,0,0); target.addView(txt("Cargando…"))
        Thread { try {
            val c=URL(url).openConnection() as HttpURLConnection; c.connectTimeout=15000; c.readTimeout=20000; c.setRequestProperty("Accept","application/json"); c.setRequestProperty("User-Agent","CajaEnmanuel/1.0")
            if(c.responseCode !in 200..299) throw Exception("Servicio no disponible (${c.responseCode})")
            val result=c.inputStream.bufferedReader().use { it.readText() }; c.disconnect()
            runOnUiThread { if(token==generation) { target.removeAllViews(); try { render(result) } catch (_:Exception) { target.addView(txt("No se pudo interpretar la respuesta.")) } } }
        } catch(e:Exception) { runOnUiThread { if(token==generation) { target.removeAllViews(); target.addView(txt("No se pudo cargar. Revisa tu conexión y vuelve a intentar.")) } } } }.start()
    }
    private fun results():LinearLayout = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; body.addView(this) }
    private fun add(target:LinearLayout,s:String,size:Float=16f,bold:Boolean=false){target.addView(txt(s,size,bold))}
    private fun url(s:String)=URLEncoder.encode(s.trim(),"UTF-8")
    private fun open(link:String) { try { val u=Uri.parse(link); if(u.scheme=="https" || u.scheme=="http") startActivity(Intent(Intent.ACTION_VIEW,u)) } catch (_:Exception) { Toast.makeText(this,"No se pudo abrir el enlace",Toast.LENGTH_SHORT).show() } }
    private fun home(){ setup("Caja de herramientas",false); label("Una colección de servicios útiles para explorar."); imageResource(R.drawable.toolbox,210); label("Elige una herramienta",18f,true)
        button("Predicción de género") { gender() }; button("Persona aleatoria") { person() }; button("Universidades por país") { universities() }; button("Clima en República Dominicana") { weather() }; button("Pokémon") { pokemon() }; button("Noticias WordPress") { news() }; button("Acerca de mí") { about() }
    }
    private fun gender(){ setup("Predicción de género"); label("Escribe un nombre para consultar Genderize."); val field=input("Ejemplo: Irma"); val out=results(); button("Consultar") {
        val q=field.text.toString().trim(); if(q.isEmpty()){ field.error="Escribe un nombre"; return@button }
        get("https://api.genderize.io/?name=${url(q)}",out){ raw-> val o=JSONObject(raw); val gender=o.optString("gender",""); if(gender=="male"||gender=="female") { val male=gender=="male"; out.background=shape(if(male) Color.rgb(219,234,254) else Color.rgb(252,223,236)); out.setPadding(dp(15),dp(10),dp(15),dp(10)); add(out,"Nombre: ${o.optString("name")}",20f,true); add(out,"Predicción: ${if(male) "Masculino" else "Femenino"}"); add(out,"Probabilidad estimada: ${(o.optDouble("probability",0.0)*100).toInt()}%") } else add(out,"No hay una predicción disponible para ese nombre.") }
    } }
    private fun person(){ setup("Persona aleatoria"); val out=results(); fun load(){ get("https://randomuser.me/api/?results=1",out){ raw-> val u=JSONObject(raw).getJSONArray("results").getJSONObject(0); val name=u.getJSONObject("name"); val dob=u.getJSONObject("dob"); val date=dob.optString("date").take(10); add(out,"${name.optString("first")} ${name.optString("last")}",21f,true); add(out,"Fecha de nacimiento: $date"); add(out,"Edad: ${dob.optInt("age")} años"); val img=ImageView(this).apply{scaleType=ImageView.ScaleType.FIT_CENTER}; out.addView(img,LinearLayout.LayoutParams(-1,dp(180))); val token=generation; val src=u.getJSONObject("picture").optString("large"); Thread { try { val bm=URL(src).openStream().use{BitmapFactory.decodeStream(it)}; runOnUiThread{if(token==generation)img.setImageBitmap(bm)} }catch(_:Exception){} }.start() } }; button("Generar otra persona") { load() }; load() }
    private fun universities(){ setup("Universidades"); label("Escribe el país en inglés, por ejemplo Dominican Republic."); val field=input("País en inglés"); val out=results(); button("Buscar universidades") { val q=field.text.toString().trim(); if(q.isEmpty()){field.error="Escribe un país";return@button}; get("https://adamix.net/proxy.php?country=${url(q)}",out){raw-> val data=JSONArray(raw); if(data.length()==0)add(out,"No se encontraron universidades.") else { add(out,"Resultados: ${data.length()}",16f,true); for(i in 0 until data.length()){ val u=data.getJSONObject(i); val domains=u.optJSONArray("domains"); val sites=u.optJSONArray("web_pages"); val site=sites?.optString(0).orEmpty(); add(out,"${i+1}. ${u.optString("name")}",17f,true); add(out,"Dominio: ${domains?.optString(0) ?: "No disponible"}",14f); if(site.isNotEmpty()) { val link=txt("Visitar: $site",14f); link.setTextColor(blue); link.setOnClickListener{open(site)}; out.addView(link) } } } } } }
    private fun weather(){ setup("Clima en República Dominicana"); label("Pronóstico de hoy en Santo Domingo."); val out=results(); fun load(){ get("https://api.open-meteo.com/v1/forecast?latitude=18.4861&longitude=-69.9312&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=America%2FSanto_Domingo&forecast_days=1",out){raw-> val d=JSONObject(raw).getJSONObject("daily"); val code=d.getJSONArray("weather_code").optInt(0); val desc=when(code){0->"Despejado";1,2,3->"Parcialmente nublado";45,48->"Niebla";51,53,55,56,57->"Llovizna";61,63,65,66,67,80,81,82->"Lluvia";95,96,99->"Tormenta";else->"Condiciones variables"}; add(out,"${d.getJSONArray("time").getString(0)} · $desc",20f,true); add(out,"Máxima: ${d.getJSONArray("temperature_2m_max").getDouble(0)} °C"); add(out,"Mínima: ${d.getJSONArray("temperature_2m_min").getDouble(0)} °C"); add(out,"Probabilidad de lluvia: ${d.getJSONArray("precipitation_probability_max").optInt(0)}%"); add(out,"Fuente: Open-Meteo",13f) } }; button("Actualizar clima") { load() }; load() }
    private fun pokemon(){ setup("Pokémon"); label("Busca por nombre o número."); val field=input("Ejemplo: pikachu"); val out=results(); button("Buscar Pokémon") { val q=field.text.toString().trim().lowercase(Locale.ROOT); if(q.isEmpty()){field.error="Escribe un Pokémon";return@button}; get("https://pokeapi.co/api/v2/pokemon/${url(q)}",out){raw-> val p=JSONObject(raw); add(out,p.optString("name").replaceFirstChar{it.titlecase(Locale.ROOT)},22f,true); add(out,"Experiencia base: ${p.optInt("base_experience")}"); val skills=p.getJSONArray("abilities"); val list=(0 until skills.length()).joinToString(", "){skills.getJSONObject(it).getJSONObject("ability").getString("name")}; add(out,"Habilidades: $list"); val image=ImageView(this).apply{scaleType=ImageView.ScaleType.FIT_CENTER}; out.addView(image,LinearLayout.LayoutParams(-1,dp(160))); val token=generation; val src=p.getJSONObject("sprites").optString("front_default"); Thread{ try { val bm=URL(src).openStream().use{BitmapFactory.decodeStream(it)}; runOnUiThread{if(token==generation)image.setImageBitmap(bm)} }catch(_:Exception){} }.start(); val sound=p.optJSONObject("cries")?.optString("latest").orEmpty(); if(sound.startsWith("https://")){ out.addView(Button(this).apply{text="Reproducir sonido";isAllCaps=false;setOnClickListener{ try { player?.release(); player=MediaPlayer().apply{setDataSource(sound);setOnPreparedListener{it.start()};setOnErrorListener{_,_,_->Toast.makeText(this@MainActivity,"No se pudo reproducir el sonido",Toast.LENGTH_SHORT).show();true};prepareAsync()}; Toast.makeText(this@MainActivity,"Preparando sonido…",Toast.LENGTH_SHORT).show() }catch(_:Exception){Toast.makeText(this@MainActivity,"No se pudo reproducir el sonido",Toast.LENGTH_SHORT).show()} }}) } else add(out,"Este Pokémon no tiene sonido disponible.") } } }
    private fun news(){ setup("Noticias de WordPress"); imageResource(R.drawable.wordpress,85); label("Últimas tres noticias de WordPress.org"); val out=results(); fun load(){get("https://wordpress.org/news/wp-json/wp/v2/posts?per_page=3&_fields=title,excerpt,link",out){raw-> val arr=JSONArray(raw); if(arr.length()==0)add(out,"No hay noticias disponibles."); for(i in 0 until arr.length()){val p=arr.getJSONObject(i); val title=Html.fromHtml(p.getJSONObject("title").optString("rendered"),Html.FROM_HTML_MODE_LEGACY).toString().trim(); val excerpt=Html.fromHtml(p.getJSONObject("excerpt").optString("rendered"),Html.FROM_HTML_MODE_LEGACY).toString().trim(); add(out,title,18f,true); add(out,excerpt,14f); val v=txt("Visitar noticia original →",14f,true);v.setTextColor(blue);v.setOnClickListener{open(p.optString("link"))};out.addView(v)} } }; button("Actualizar noticias") { load() }; load() }
    private fun about(){ setup("Acerca de mí"); imageResource(R.drawable.foto,160); label("Enmanuel Chavez",22f,true); label("Matrícula: 2025-1099"); label("Correo: 20251099@itla.edu.do"); label("LinkedIn: Enmanuel Chavez"); label("Foto de portada: Per Erik Strandberg / Wikimedia Commons, CC BY-SA 2.5",12f); button("Créditos de la fotografía") { open("https://commons.wikimedia.org/wiki/File:20060513_toolbox.jpg") }; button("Enviar correo") { try { startActivity(Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:20251099@itla.edu.do"))) }catch(_:Exception){Toast.makeText(this,"No hay una aplicación de correo",Toast.LENGTH_SHORT).show()} } }
}
