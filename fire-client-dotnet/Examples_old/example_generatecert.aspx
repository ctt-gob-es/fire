<%@ Page Language="C#" AutoEventWireup="true" CodeFile="example_generatecert.aspx.cs" Inherits="example_generatecert" %>

<%@ Register tagprefix="cf" Namespace="FIRe" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Ejemplo de generacion de un nuevo certificado de firma</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <br /><asp:Label runat="server" id="ErrorMsg"></asp:Label>
        <br /><b>Id de transaccion:</b> <asp:Label runat="server" id="TransactionId"></asp:Label>
        <br /><b>URL a la que redirigir al usuario:</b> <asp:Label runat="server" id="RedirectionURL"></asp:Label>

    </div>
    </form>
</body>
</html>
