package fusion.developer.web.cavidades.model;

import java.util.ArrayList;
import java.util.List;

public class Molde {
    public int id;
    public String codigo;
    public String produto;
    public String modelo;
    public int totalCavidades;
    public int numZonas;
    public int cavidadesPorZona;
    public String tipoLayout;
    public String imagemPecaUrl;
    public String svgLayoutUrl;
    public String material;
    public String maquina;
    public List<Cavidade> cavidades = new ArrayList<>();
}