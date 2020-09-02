<%@ Page Language="C#" AutoEventWireup="true" CodeFile="example_recovercert.aspx.cs" Inherits="example_recovercert" %>

<%@ Register tagprefix="cf" Namespace="FIRe" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Ejemplo de recuperacion del certificado recien generado</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <span><b>Certificado Base64: </b><asp:Label runat="server" id="Cert1B64"></asp:Label></span>
        
        <br /><br /><br />

        <b><asp:Label runat="server" id="Cert1SubjectCN"></asp:Label></b><br />
        <span>Emisor: <asp:Label runat="server" id="Cert1IssuerCN"></asp:Label></span><br />
        <span>Fecha caducidad: <asp:Label runat="server" id="Cert1ExpirationDate"></asp:Label></span><br />
    </div>
    </form>
</body>
</html>
