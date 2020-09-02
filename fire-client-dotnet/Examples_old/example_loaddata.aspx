<%@ Page Language="C#" AutoEventWireup="true" CodeFile="example_loaddata.aspx.cs" Inherits="example_loaddata" %>

<%@ Register tagprefix="cf" Namespace="FIRe" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Ejemplo de carga de datos</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
    
        <br /><b>Id de transaccion:</b> <asp:Label runat="server" id="TransactionId"></asp:Label>
        <br /><b>URL a la que redirigir al usuario:</b> <asp:Label runat="server" id="RedirectionURL"></asp:Label>
        <br /><b>Resultado parcial de la firma trifasica:</b> <asp:Label runat="server" id="TriphaseData"></asp:Label>
        <br />
    </div>
    </form>
</body>
</html>
