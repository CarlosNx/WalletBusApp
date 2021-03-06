package com.walletbus.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.walletbus.R;
import com.walletbus.config.ConfiguracaoFirebase;
import com.walletbus.helper.Base64Custom;
import com.walletbus.helper.DateCustom;
import com.walletbus.helper.Mask;
import com.walletbus.model.Movimentacao;
import com.walletbus.model.Usuario;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class RecargaActivity extends AppCompatActivity {

    private EditText editValorRecarga;
    private TextView campoData;
    private Button buttonRecarga;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double saldoAtualizado;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        editValorRecarga = findViewById(R.id.editValorRecarga);
        campoData = findViewById(R.id.editData);
        buttonRecarga = findViewById(R.id.btnrecarregar);

        //MASCARA
        TextWatcher money = Mask.monetario(editValorRecarga);
        editValorRecarga.addTextChangedListener(money);

        //Preencher o campo com data atual
        campoData.setText(DateCustom.dataAtual());
        recuperarSaldoAtualizado();


    }

    public void recarregarCartao(View view) {

        //VALIDACAO E ATUALIZAÇÃO DO SALDO COM O VALOR RECARREGADO
        if (validarCamposRecargas()){
            movimentacao = new Movimentacao();
            String data = campoData.getText().toString();
            String categoria = "Estudante";//Categoria do cartão
            Double valorRecuperado = Double.parseDouble(editValorRecarga.getText().toString());
            movimentacao.setValor(valorRecuperado);
            movimentacao.setData(data);

            Double saldoAtual = saldoAtualizado + valorRecuperado;//VALOR RECUPERADO NO CAMPO VALOR RECARGA
            atualizarSaldo( saldoAtual );
            movimentacao.salvar(data);
            finish();//FECHAR APOS RECARREGAR

        }

    }
    public Boolean validarCamposRecargas(){
        String textoValor = editValorRecarga.getText().toString();

        if ( !textoValor.isEmpty()){
            Toasty.success(RecargaActivity.this,"Recarga efetuada com sucesso!",Toasty.LENGTH_SHORT).show();
            return true;
        }else{
            Toasty.error(RecargaActivity.this,"Valor não foi preenchido",Toasty.LENGTH_SHORT).show();
            return false;
        }
    }

public void recuperarSaldoAtualizado(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue( Usuario.class );
                saldoAtualizado = usuario.getSaldo();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
}
    public void atualizarSaldo(Double saldoAtualizado){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        usuarioRef.child("saldo").setValue(saldoAtualizado);

    }


}

