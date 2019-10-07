<%@ Page Language="C#" AutoEventWireup="true" CodeFile="example_fire_recover_sign.aspx.cs" Inherits="example_fire_recover_sign" %>

<%@ Register tagprefix="cf" Namespace="FIRe" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Recuperar documento firmado</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <br /><b>Proveedor:</b><br /> <asp:Label runat="server" id="Provider"></asp:Label>
        <br /><b>Certificado:</b><br /> <asp:Label runat="server" id="CertB64"></asp:Label>
        <br /><b>Firma:</b><br /> <asp:Label runat="server" id="SignatureB64"></asp:Label>
    </div>
    </form>
</body>
</html>
