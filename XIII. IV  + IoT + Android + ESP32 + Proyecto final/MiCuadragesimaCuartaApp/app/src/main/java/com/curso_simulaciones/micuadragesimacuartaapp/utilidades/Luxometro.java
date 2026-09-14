package com.curso_simulaciones.micuadragesimacuartaapp.utilidades;

import android.content.Context;

public class Luxometro extends GaugeSimple {



    public Luxometro(Context context){
        super(context);


    }


    public void cambiarEscala(float medida){

        float maximo=100f;
        float minimo=0f;

        if(medida>0 && ((medida < 100)^(medida==100f ))){

            maximo=100f;
            minimo=0f;

        }

        if(medida>100 && ((medida < 1000)^(medida==500f ))){

            maximo=500f;
            minimo=0f;

        }

        if(medida>500 && ((medida < 1000)^(medida==1000f ))){

            maximo=1000f;
            minimo=0f;

        }


        if(medida>1000 && ((medida < 5000)^(medida==5000f ))){

            maximo=5000f;
            minimo=0f;

        }

        if(medida>5000 && ((medida < 10000)^(medida==10000f ))){

            maximo=10000f;
            minimo=0f;

        }

        if(medida>10000 && ((medida < 50000)^(medida==50000f ))){

            maximo=50000f;
            minimo=0f;

        }


        this.setRango(minimo,maximo);

    }


}